package com.giacomoferretti.odexpatcher.ui.main

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giacomoferretti.odexpatcher.data.PatchState
import com.giacomoferretti.odexpatcher.utils.AppInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random.Default.nextLong

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var patchJob: Job? = null

    fun setMessageShown(messageId: Long) {
        _uiState.update { state ->
            state.copy(userMessages = state.userMessages.filterNot {
                it.id == messageId
            })
        }
        /*_uiState.update { currentMessages ->
            currentMessages.filterNot { it.id == messageId }
        }
        UUID.randomUUID().mostSignificantBits*/
    }

    fun showRandomMessage() {
        _uiState.update { state ->
            state.copy(
                userMessages = state.userMessages + Message(
                    id = UUID.randomUUID().mostSignificantBits,
                    message = UUID.randomUUID().toString()
                )
            )
        }
    }

    fun patch() {
        patchJob?.cancel()
        patchJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isPatching = true, progress = 0).goToStep(PatchState.READY)
            }

            for (i in PatchState.values().copyOfRange(1, PatchState.values().size - 1)) {
                _uiState.update {
                    it.nextStep()
                }
                delay(nextLong(from = 500, until = 1000))
            }

            _uiState.update {
                it.copy(isPatching = false, progress = 100).goToStep(PatchState.PATCHED)
            }
        }
    }

    fun debugInit(packageManager: PackageManager, inputPackage: String, outputPackage: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    inputApp = AppInfo.fromPackageName(
                        packageManager,
                        inputPackage
                    )
                )
            }

            _uiState.update {
                it.copy(
                    outputApp = AppInfo.fromPackageName(
                        packageManager,
                        outputPackage
                    )
                )
            }
        }
    }
}