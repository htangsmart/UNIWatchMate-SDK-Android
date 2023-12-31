package com.sjbt.sdk.sample.ui.device.alarm

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted

/**
 * Dialog wait alarm changes saving
 */
class SetAlarmsDialogFragment : AppCompatDialogFragment() {

    private val viewModel: AlarmViewModel by viewModels({ requireParentFragment().requireParentFragment() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.ds_alarm)
            .setMessage(R.string.tip_setting_loading)
            .setNegativeButton(android.R.string.cancel) { _, _ ->
//                viewModel.setAlarmsAction.cancel()
            }
            .setPositiveButton(R.string.action_retry, null)
            .create()
        lifecycle.launchRepeatOnStarted {
//            viewModel.setAlarmsAction.flowState.collect {
//                when (it) {
//                    is Loading -> {
//                        dialog.setMessage(getString(R.string.tip_setting_loading))
//                        val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
//                        positiveButton.isVisible = false
//                    }
//
//                    is Fail -> {
//                        dialog.setMessage(getString(R.string.tip_setting_fail))
//                        val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
//                        positiveButton.isVisible = true
//                        positiveButton.setOnClickListener {
//                            viewModel.setAlarmsAction.retry()
//                        }
//                    }
//
//                    is Success -> {
//                        dialog.setMessage(getString(R.string.tip_setting_success))
//                        viewModel.sendNavigateUpEvent()
//                    }
//
//                    else -> {}
//                }
//            }
        }
        return dialog
    }
}