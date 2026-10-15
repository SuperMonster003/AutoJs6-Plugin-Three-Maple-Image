package io.github.supermonster003.autojs6.plugin.three.maple.image

/**
 * A field of the extended details over the viewer (ROADMAP 9.1), in the order they are shown. [key] is what the
 * settings store, so renaming an entry keeps the choice.
 */
internal enum class ExtendedDetailField(val key: String, val group: Group) {
    PATH("path", Group.FILE_PATH),
    RESOLUTION("resolution", Group.FILE),
    SIZE("size", Group.FILE),
    TAKEN("taken", Group.FILE),
    MODIFIED("modified", Group.FILE),
    CAMERA("camera", Group.CAMERA),
    EXPOSURE("exposure", Group.CAMERA),
    FOCAL_LENGTH("focal-length", Group.CAMERA),
    ISO("iso", Group.CAMERA),
    LOCATION("location", Group.LOCATION),
    ;

    /** Fields of a group share a line; the path and the location have one each. */
    enum class Group { FILE_PATH, FILE, CAMERA, LOCATION }
}

/** The formatted values of one page; null where the page has none (no EXIF, not from the media library). */
internal data class ExtendedDetailValues(
    val path: String? = null,
    val resolution: String? = null,
    val size: String? = null,
    val taken: String? = null,
    val modified: String? = null,
    val camera: String? = null,
    val exposure: String? = null,
    val focalLength: String? = null,
    val iso: String? = null,
    val location: String? = null,
) {
    fun of(field: ExtendedDetailField): String? = when (field) {
        ExtendedDetailField.PATH -> path
        ExtendedDetailField.RESOLUTION -> resolution
        ExtendedDetailField.SIZE -> size
        ExtendedDetailField.TAKEN -> taken
        ExtendedDetailField.MODIFIED -> modified
        ExtendedDetailField.CAMERA -> camera
        ExtendedDetailField.EXPOSURE -> exposure
        ExtendedDetailField.FOCAL_LENGTH -> focalLength
        ExtendedDetailField.ISO -> iso
        ExtendedDetailField.LOCATION -> location
    }?.takeIf { it.isNotBlank() }
}

internal object ExtendedDetailsPolicy {

    /** What "Extended details" starts with when it is first opened. */
    val SUGGESTED: Set<ExtendedDetailField> = setOf(
        ExtendedDetailField.RESOLUTION,
        ExtendedDetailField.SIZE,
        ExtendedDetailField.TAKEN,
        ExtendedDetailField.CAMERA,
    )

    fun encode(fields: Set<ExtendedDetailField>): String =
        ExtendedDetailField.entries.filter { it in fields }.joinToString(SEPARATOR) { it.key }

    /** Unknown keys (from a later version) are ignored; nothing stored means the details are off. */
    fun decode(stored: String?): Set<ExtendedDetailField> = stored.orEmpty().split(SEPARATOR)
        .mapNotNull { key -> ExtendedDetailField.entries.firstOrNull { it.key == key.trim() } }
        .toSet()

    /** The location is offered only while "Show location" is on (T14); otherwise it is never shown. */
    fun available(field: ExtendedDetailField, showLocation: Boolean): Boolean =
        field != ExtendedDetailField.LOCATION || showLocation

    /**
     * The lines over the viewer: chosen fields in their order, one line per group joined with " · ", values the page
     * does not have left out.
     */
    fun lines(fields: Set<ExtendedDetailField>, values: ExtendedDetailValues, showLocation: Boolean): List<String> =
        ExtendedDetailField.entries
            .filter { it in fields && available(it, showLocation) }
            .mapNotNull { field -> values.of(field)?.let { field.group to it } }
            .groupBy({ it.first }, { it.second })
            .values
            .map { it.joinToString(VALUE_SEPARATOR) }

    private const val SEPARATOR = ","
    private const val VALUE_SEPARATOR = " · "
}
