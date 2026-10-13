package io.github.supermonster003.autojs6.plugin.three.maple.image

/** Selection rules of the media grid (ROADMAP 7.1); Android-free. Sets keep the order ids were added in. */
internal object MediaSelection {

    fun toggle(selection: Set<Long>, id: Long): Set<Long> =
        LinkedHashSet(selection).apply { if (!remove(id)) add(id) }

    /**
     * Drag selection: [base] (the selection when the long press started) plus every id from [anchor] to [target]
     * in grid order, in either direction. Moving back toward the anchor shrinks the range again; ids of [base]
     * stay selected. An id that is no longer in [ordered] only adds [target].
     */
    fun range(base: Set<Long>, ordered: List<Long>, anchor: Long, target: Long): Set<Long> {
        val from = ordered.indexOf(anchor)
        val to = ordered.indexOf(target)
        val result = LinkedHashSet(base)
        if (from < 0 || to < 0) {
            result += anchor
            result += target
            return result
        }
        result.addAll(ordered.subList(minOf(from, to), maxOf(from, to) + 1))
        return result
    }

    /** Everything of [all] that is not selected, in album order. */
    fun invert(all: List<Long>, selection: Set<Long>): Set<Long> = all.filterTo(LinkedHashSet()) { it !in selection }
}
