package io.github.supermonster003.autojs6.plugin.three.maple.image

import java.util.Locale

/** Why a proposed file name was refused (ROADMAP 7.3 rename). */
internal enum class FileNameProblem { BLANK, ILLEGAL_CHARACTER, RESERVED, TOO_LONG, LEADING_DOT }

internal sealed interface FileNameCheck {
    /** [name] is acceptable; [extensionChanged] asks the user to confirm a different or missing extension. */
    data class Valid(val name: String, val extensionChanged: Boolean, val unchanged: Boolean) : FileNameCheck
    data class Invalid(val problem: FileNameProblem) : FileNameCheck
}

/** File name rules shared by rename, new albums and conflict suffixes; Android-free. */
internal object MediaFileNamePolicy {
    private const val MAX_BYTES = 255
    private val illegal = Regex("""[/\\:*?"<>|\u0000-\u001f\u007f]""")

    fun check(currentName: String, proposed: String): FileNameCheck {
        val name = proposed.trim()
        if (name.isEmpty()) return FileNameCheck.Invalid(FileNameProblem.BLANK)
        if (illegal.containsMatchIn(name)) return FileNameCheck.Invalid(FileNameProblem.ILLEGAL_CHARACTER)
        if (name == "." || name == "..") return FileNameCheck.Invalid(FileNameProblem.RESERVED)
        if (name.startsWith(".")) return FileNameCheck.Invalid(FileNameProblem.LEADING_DOT)
        if (name.toByteArray(Charsets.UTF_8).size > MAX_BYTES) return FileNameCheck.Invalid(FileNameProblem.TOO_LONG)
        val extensionChanged = extension(currentName) != extension(name)
        return FileNameCheck.Valid(name, extensionChanged, unchanged = name == currentName)
    }

    /** A folder name for "new album" (`Pictures/<name>`) or an album rename, with the same character rules. */
    fun checkFolder(proposed: String, currentName: String = ""): FileNameCheck = when (val result = check(currentName, proposed)) {
        is FileNameCheck.Valid -> result.copy(extensionChanged = false)
        is FileNameCheck.Invalid -> result
    }

    /** `Pictures/Trips/` renamed to [name]: `Pictures/<name>/` (the parent stays). */
    fun renamedFolder(relativePath: String, name: String): String {
        val parent = relativePath.trimEnd('/').substringBeforeLast('/', missingDelimiterValue = "")
        return if (parent.isEmpty()) "$name/" else "$parent/$name/"
    }

    fun extension(name: String): String = name.substringAfterLast('.', "").lowercase(Locale.ROOT).takeIf { name.contains('.') }.orEmpty()

    fun baseName(name: String): String = if (name.contains('.')) name.substringBeforeLast('.') else name
}

/** What to do when a moved / copied / restored file meets an existing name (ROADMAP 7.3). */
internal enum class NameConflictChoice { AUTO_RENAME, SKIP, REPLACE }

internal object NameConflictPolicy {
    /**
     * [desired] if no existing name equals it ignoring case (Android's shared storage is case-insensitive),
     * otherwise `base (n).ext` with the smallest free n.
     */
    fun uniqueName(desired: String, existing: Collection<String>): String {
        val taken = existing.mapTo(HashSet()) { it.lowercase(Locale.ROOT) }
        if (desired.lowercase(Locale.ROOT) !in taken) return desired
        val base = MediaFileNamePolicy.baseName(desired)
        val extension = MediaFileNamePolicy.extension(desired).let { if (it.isEmpty()) "" else ".${desired.substringAfterLast('.')}" }
        var counter = 1
        while (true) {
            val candidate = "$base ($counter)$extension"
            if (candidate.lowercase(Locale.ROOT) !in taken) return candidate
            counter++
        }
    }
}

/** One entry of the private recycle bin as the policy sees it. */
internal data class RecycleBinItem(val id: Long, val sizeBytes: Long, val deletedAtMillis: Long)

internal data class RecycleBinPlan(
    /** Entries to delete for good before the incoming files go in: expired ones, then the oldest until it fits. */
    val purgeIds: List<Long>,
    /** False when the incoming files alone exceed the capacity: they are deleted permanently after a confirmation. */
    val fits: Boolean,
)

/** Private recycle bin of Android 7-10 (ROADMAP T10, Q8); Android-free. */
internal object RecycleBinPolicy {
    const val RETENTION_DAYS = 30
    const val MAX_CAPACITY_BYTES = 2L * 1024 * 1024 * 1024
    private const val DAY_MILLIS = 24L * 60 * 60 * 1000

    /** 10% of the free space, at most 2 GiB. */
    fun capacity(freeBytes: Long): Long = minOf(maxOf(freeBytes, 0L) / 10, MAX_CAPACITY_BYTES)

    fun isExpired(deletedAtMillis: Long, nowMillis: Long): Boolean = nowMillis - deletedAtMillis >= RETENTION_DAYS * DAY_MILLIS

    /** Whole days left before the entry expires, never negative. */
    fun remainingDays(deletedAtMillis: Long, nowMillis: Long): Int {
        val left = deletedAtMillis + RETENTION_DAYS * DAY_MILLIS - nowMillis
        return if (left <= 0) 0 else ((left + DAY_MILLIS - 1) / DAY_MILLIS).toInt()
    }

    /** Cleanup at start-up ([incomingBytes] = 0) and before each delete. */
    fun plan(entries: List<RecycleBinItem>, freeBytes: Long, incomingBytes: Long, nowMillis: Long): RecycleBinPlan {
        val capacity = capacity(freeBytes)
        val purge = ArrayList<Long>()
        val kept = ArrayList<RecycleBinItem>()
        for (entry in entries) if (isExpired(entry.deletedAtMillis, nowMillis)) purge += entry.id else kept += entry
        if (incomingBytes > capacity) return RecycleBinPlan(purge, fits = false)
        var total = kept.sumOf { it.sizeBytes } + incomingBytes
        for (entry in kept.sortedWith(compareBy<RecycleBinItem> { it.deletedAtMillis }.thenBy { it.id })) {
            if (total <= capacity) break
            purge += entry.id
            total -= entry.sizeBytes
        }
        return RecycleBinPlan(purge, fits = true)
    }

    /** File name inside the private folder: unique, and keeps the original name readable for diagnostics. */
    fun storedName(displayName: String, deletedAtMillis: Long, existing: Collection<String>): String =
        NameConflictPolicy.uniqueName("$deletedAtMillis-$displayName", existing)
}

internal enum class MediaWriteOperation { TRASH, RESTORE, DELETE, RENAME, MOVE, COPY, FAVORITE }

/** How one batch reaches the files (ROADMAP 7.1 `MediaWriteCoordinator`). */
internal enum class MediaWriteRoute {
    /** Update or delete through the content resolver without asking. */
    DIRECT,
    /** One system confirmation for the batch (`createTrashRequest` / `createDeleteRequest` / `createWriteRequest`). */
    SYSTEM_REQUEST,
    /** `java.io.File` operations plus a media scan (Android 9-10 with legacy storage). */
    LEGACY_FILE,
    /** Android 9-10 recycle: the file moves into the private recycle bin. */
    PRIVATE_BIN,
    /** Android 9-10 without the storage write permission: ask for it first. */
    NEEDS_LEGACY_PERMISSION,
}

internal data class MediaWriteFacts(
    val sdk: Int,
    val allFilesAccess: Boolean,
    /** `MediaStore.canManageMedia` (Android 12+ media management special access, Q decision 2026-10-13). */
    val manageMedia: Boolean,
    val legacyWriteGranted: Boolean,
    /** Every item of the batch was created by this app (no confirmation needed on Android 10+). */
    val ownedByApp: Boolean = false,
)

/** Chooses the route of a write batch; Android-free so every API level is covered by JVM tests. */
internal object MediaWritePolicy {
    private const val R = 30

    fun route(operation: MediaWriteOperation, facts: MediaWriteFacts): MediaWriteRoute {
        if (facts.sdk < R) {
            if (operation == MediaWriteOperation.COPY && facts.sdk >= 29) return MediaWriteRoute.DIRECT
            if (!facts.legacyWriteGranted && !facts.allFilesAccess) return MediaWriteRoute.NEEDS_LEGACY_PERMISSION
            return when (operation) {
                MediaWriteOperation.TRASH -> MediaWriteRoute.PRIVATE_BIN
                MediaWriteOperation.RESTORE -> MediaWriteRoute.PRIVATE_BIN
                MediaWriteOperation.COPY -> MediaWriteRoute.DIRECT
                else -> MediaWriteRoute.LEGACY_FILE
            }
        }
        if (operation == MediaWriteOperation.COPY) return MediaWriteRoute.DIRECT
        if (facts.ownedByApp) return MediaWriteRoute.DIRECT
        // With media management access the system grants the request without showing it; all files access may
        // write directly (the coordinator falls back to a request when the provider still refuses).
        if (facts.allFilesAccess && !facts.manageMedia) return MediaWriteRoute.DIRECT
        return MediaWriteRoute.SYSTEM_REQUEST
    }
}
