package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** ROADMAP 7.1-7.3: rename rules, conflict names, the API 24-29 recycle bin and the write route matrix. */
class MediaManagementPolicyTest {

    @Test
    fun renameRejectsBlankIllegalReservedHiddenAndOverlongNames() {
        assertEquals(FileNameCheck.Invalid(FileNameProblem.BLANK), MediaFileNamePolicy.check("a.jpg", "   "))
        for (name in listOf("a/b.jpg", "a\\b.jpg", "a:b.jpg", "a*b.jpg", "a?b.jpg", "a\"b.jpg", "a<b.jpg", "a>b.jpg", "a|b.jpg", "a\u0001.jpg")) {
            assertEquals(name, FileNameCheck.Invalid(FileNameProblem.ILLEGAL_CHARACTER), MediaFileNamePolicy.check("a.jpg", name))
        }
        assertEquals(FileNameCheck.Invalid(FileNameProblem.RESERVED), MediaFileNamePolicy.check("a.jpg", ".."))
        assertEquals(FileNameCheck.Invalid(FileNameProblem.LEADING_DOT), MediaFileNamePolicy.check("a.jpg", ".hidden.jpg"))
        assertEquals(FileNameCheck.Invalid(FileNameProblem.TOO_LONG), MediaFileNamePolicy.check("a.jpg", "图".repeat(86) + ".jpg"))
    }

    @Test
    fun renameReportsExtensionChangesAndNoOps() {
        assertEquals(FileNameCheck.Valid("b.jpg", extensionChanged = false, unchanged = false), MediaFileNamePolicy.check("a.jpg", " b.jpg "))
        assertEquals(FileNameCheck.Valid("a.JPG", extensionChanged = false, unchanged = false), MediaFileNamePolicy.check("a.jpg", "a.JPG"))
        assertTrue((MediaFileNamePolicy.check("a.jpg", "a.png") as FileNameCheck.Valid).extensionChanged)
        assertTrue((MediaFileNamePolicy.check("a.jpg", "a") as FileNameCheck.Valid).extensionChanged)
        assertTrue((MediaFileNamePolicy.check("a.jpg", "a.jpg") as FileNameCheck.Valid).unchanged)
        assertEquals(FileNameCheck.Valid("Trips", extensionChanged = false, unchanged = false), MediaFileNamePolicy.checkFolder("Trips"))
    }

    @Test
    fun conflictsGetTheSmallestFreeSuffixIgnoringCase() {
        assertEquals("a.jpg", NameConflictPolicy.uniqueName("a.jpg", listOf("b.jpg")))
        assertEquals("a (1).jpg", NameConflictPolicy.uniqueName("a.jpg", listOf("A.JPG")))
        assertEquals("a (3).jpg", NameConflictPolicy.uniqueName("a.jpg", listOf("a.jpg", "a (1).jpg", "a (2).JPG")))
        assertEquals("notes (1)", NameConflictPolicy.uniqueName("notes", listOf("notes")))
        assertEquals("x.tar (1).gz", NameConflictPolicy.uniqueName("x.tar.gz", listOf("x.tar.gz")))
    }

    @Test
    fun recycleBinCapacityIsTenPercentOfFreeSpaceCappedAtTwoGib() {
        assertEquals(100L, RecycleBinPolicy.capacity(1_000))
        assertEquals(RecycleBinPolicy.MAX_CAPACITY_BYTES, RecycleBinPolicy.capacity(100L * 1024 * 1024 * 1024))
        assertEquals(0L, RecycleBinPolicy.capacity(-5))
    }

    @Test
    fun recycleBinExpiresAfterThirtyDaysAndCountsRemainingDays() {
        val day = 24L * 60 * 60 * 1000
        assertFalse(RecycleBinPolicy.isExpired(0, 30 * day - 1))
        assertTrue(RecycleBinPolicy.isExpired(0, 30 * day))
        assertEquals(30, RecycleBinPolicy.remainingDays(0, 0))
        assertEquals(1, RecycleBinPolicy.remainingDays(0, 29 * day + 1))
        assertEquals(0, RecycleBinPolicy.remainingDays(0, 31 * day))
    }

    @Test
    fun recycleBinPurgesExpiredThenOldestUntilTheIncomingFilesFit() {
        val day = 24L * 60 * 60 * 1000
        val now = 40 * day
        val entries = listOf(
            RecycleBinItem(id = 1, sizeBytes = 30, deletedAtMillis = 5 * day), // expired
            RecycleBinItem(id = 2, sizeBytes = 40, deletedAtMillis = 20 * day),
            RecycleBinItem(id = 3, sizeBytes = 40, deletedAtMillis = 25 * day),
            RecycleBinItem(id = 4, sizeBytes = 10, deletedAtMillis = 35 * day),
        )
        // capacity 100: kept 90 + incoming 30 = 120 -> drop the oldest kept (id 2) -> 80
        assertEquals(RecycleBinPlan(listOf(1L, 2L), fits = true), RecycleBinPolicy.plan(entries, freeBytes = 1_000, incomingBytes = 30, nowMillis = now))
        assertEquals(RecycleBinPlan(listOf(1L), fits = true), RecycleBinPolicy.plan(entries, freeBytes = 1_000, incomingBytes = 0, nowMillis = now))
        assertEquals(RecycleBinPlan(listOf(1L), fits = false), RecycleBinPolicy.plan(entries, freeBytes = 1_000, incomingBytes = 101, nowMillis = now))
    }

    @Test
    fun storedNamesStayUnique() {
        assertEquals("7-a.jpg", RecycleBinPolicy.storedName("a.jpg", 7, emptyList()))
        assertEquals("7-a (1).jpg", RecycleBinPolicy.storedName("a.jpg", 7, listOf("7-a.jpg")))
    }

    @Test
    fun writeRoutesFollowTheApiLevelAndTheGrantedAccess() {
        fun facts(sdk: Int, allFiles: Boolean = false, manage: Boolean = false, legacy: Boolean = true, owned: Boolean = false) =
            MediaWriteFacts(sdk, allFiles, manage, legacy, owned)
        // Android 7-10: private recycle bin and java.io.File, after the legacy write permission.
        assertEquals(MediaWriteRoute.PRIVATE_BIN, MediaWritePolicy.route(MediaWriteOperation.TRASH, facts(28)))
        assertEquals(MediaWriteRoute.PRIVATE_BIN, MediaWritePolicy.route(MediaWriteOperation.RESTORE, facts(24)))
        assertEquals(MediaWriteRoute.LEGACY_FILE, MediaWritePolicy.route(MediaWriteOperation.MOVE, facts(29)))
        assertEquals(MediaWriteRoute.LEGACY_FILE, MediaWritePolicy.route(MediaWriteOperation.DELETE, facts(28)))
        assertEquals(MediaWriteRoute.NEEDS_LEGACY_PERMISSION, MediaWritePolicy.route(MediaWriteOperation.RENAME, facts(28, legacy = false)))
        assertEquals(MediaWriteRoute.DIRECT, MediaWritePolicy.route(MediaWriteOperation.COPY, facts(29, legacy = false)))
        assertEquals(MediaWriteRoute.NEEDS_LEGACY_PERMISSION, MediaWritePolicy.route(MediaWriteOperation.COPY, facts(28, legacy = false)))
        // Android 11+: one system request per batch unless the items are ours or all files access writes directly.
        for (operation in listOf(MediaWriteOperation.TRASH, MediaWriteOperation.RESTORE, MediaWriteOperation.DELETE, MediaWriteOperation.RENAME, MediaWriteOperation.MOVE)) {
            assertEquals(MediaWriteRoute.SYSTEM_REQUEST, MediaWritePolicy.route(operation, facts(33)))
            assertEquals(MediaWriteRoute.SYSTEM_REQUEST, MediaWritePolicy.route(operation, facts(35, manage = true)))
            assertEquals(MediaWriteRoute.DIRECT, MediaWritePolicy.route(operation, facts(33, allFiles = true)))
            assertEquals(MediaWriteRoute.DIRECT, MediaWritePolicy.route(operation, facts(30, owned = true)))
        }
        assertEquals(MediaWriteRoute.DIRECT, MediaWritePolicy.route(MediaWriteOperation.COPY, facts(36, legacy = false)))
    }
}
