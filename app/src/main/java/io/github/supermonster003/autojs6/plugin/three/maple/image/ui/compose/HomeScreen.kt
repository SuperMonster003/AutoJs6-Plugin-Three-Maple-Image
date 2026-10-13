package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

/** Working state of the file-picker flows (view / edit / convert / export) that the home shows in its status line and menu. */
internal data class HomeUiState(
    val working: Boolean = false,
    val statusText: String = "",
    val pendingOutputName: String? = null,
)
