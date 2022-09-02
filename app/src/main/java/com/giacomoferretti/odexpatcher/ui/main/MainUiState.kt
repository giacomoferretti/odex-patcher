package com.giacomoferretti.odexpatcher.ui.main

import android.util.Log
import com.giacomoferretti.odexpatcher.data.PatchState
import com.giacomoferretti.odexpatcher.utils.AppInfo

data class Message(val id: Long, val message: String)

data class MainUiState(
    val isPatching: Boolean = false,
    val progress: Int = 0,
    val userMessages: List<Message> = listOf(),
    val inputApp: AppInfo? = null,
    val outputApp: AppInfo? = null,
    val patchState: PatchState = PatchState.READY
)

fun MainUiState.nextStep(): MainUiState {
    if (this.patchState.ordinal + 1 == PatchState.values().size) {
        Log.e("OdexPatcher", "Cannot go to next step. Last step reached.")
        return this
    }

    return this.copy(
        patchState = PatchState.values()[this.patchState.ordinal + 1],
        progress = this.patchState.ordinal + 1
    )
}

fun MainUiState.goToStep(step: PatchState): MainUiState {
    return this.copy(
        patchState = step,
        progress = step.ordinal
    )
}

