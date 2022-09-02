package com.giacomoferretti.odexpatcher.data

import androidx.annotation.StringRes
import com.giacomoferretti.odexpatcher.R

enum class PatchState(@StringRes val value: Int) {
    READY(R.string.patch_state_ready),
    STEP1(R.string.patch_state_step1),
    STEP2(R.string.patch_state_step2),
    STEP3(R.string.patch_state_step3),
    STEP4(R.string.patch_state_step4),
    STEP5(R.string.patch_state_step5),
    STEP6(R.string.patch_state_step6),
    STEP7(R.string.patch_state_step7),
    STEP8(R.string.patch_state_step8),
    STEP9(R.string.patch_state_step9),
    STEP10(R.string.patch_state_step10),
    STEP11(R.string.patch_state_step11),
    STEP12(R.string.patch_state_step12),
    PATCHED(R.string.patch_state_patched),
}