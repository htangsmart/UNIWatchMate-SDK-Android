package com.sjbt.sdk.sample.ui.device.dial.library

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.viewModels
import com.base.api.UNIWatchMate
import com.base.sdk.port.WmTransferState
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sjbt.sdk.sample.databinding.DialogDialLibraryDfuBinding
import com.sjbt.sdk.sample.dialog.CallBack
import com.sjbt.sdk.sample.model.user.DialMock
import com.sjbt.sdk.sample.utils.PermissionHelper
import com.sjbt.sdk.sample.utils.getParcelableCompat
import com.sjbt.sdk.sample.utils.glideShowMipmapImage
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import kotlinx.coroutines.launch

class DialLibraryDfuDialogFragment : AppCompatDialogFragment() {

    companion object {
        private const val EXTRA_DIAL_PACKET = "dial_packet"

        fun newInstance(dialPacket: DialMock): DialLibraryDfuDialogFragment {
            val fragment = DialLibraryDfuDialogFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(EXTRA_DIAL_PACKET, dialPacket)
            }
            return fragment
        }
    }

    private lateinit var dialPacket: DialMock

    private var _viewBind: DialogDialLibraryDfuBinding? = null
    private val viewBind get() = _viewBind!!

    private val dfuViewModel: DfuViewModel by viewModels({ requireParentFragment() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireArguments().let {
            dialPacket = it.getParcelableCompat(EXTRA_DIAL_PACKET)!!
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _viewBind = DialogDialLibraryDfuBinding.inflate(LayoutInflater.from(context))

        viewBind.tvName.text = dialPacket.dialAssert

        glideShowMipmapImage(viewBind.imgView, dialPacket.dialCoverRes, false)


        resetStateView()

        viewBind.stateView.clickTrigger {
//            if (!dfuViewModel.isDfuIng()) {
                PermissionHelper.requestBle(this) { granted ->
                    if (granted) {
                        dfuViewModel.startDfu(dialPacket) {
                            UNIWatchMate.wmLog.logI("DfuViewModel", it.toString())
                        }
                    }
                }
//            }
        }

        lifecycle.launchRepeatOnStarted {
            launch {
//                dfuViewModel.flowDfuStateProgress().collect {
//                    when (it.state) {
//                        FcDfuManager.DfuState.NONE, FcDfuManager.DfuState.DFU_FAIL, FcDfuManager.DfuState.DFU_SUCCESS -> {
//                            resetStateView()
//                            isCancelable = true
//                        }
//                        FcDfuManager.DfuState.DOWNLOAD_FILE -> {
//                            viewBind.stateView.setText(R.string.ds_dfu_downloading)
//                            isCancelable = false
//                        }
//                        FcDfuManager.DfuState.PREPARE_FILE, FcDfuManager.DfuState.PREPARE_DFU -> {
//                            viewBind.stateView.setText(R.string.ds_dfu_preparing)
//                            isCancelable = false
//                        }
//                        FcDfuManager.DfuState.DFU_ING -> {
//                            viewBind.stateView.setText(R.string.ds_dfu_pushing)
//                            isCancelable = false
//                        }
//                    }
//                    viewBind.stateView.progress = it.progress
//                }
            }
            launch {
//                dfuViewModel.flowDfuEvent.collect {
//                    when (it) {
//                        is DfuViewModel.DfuEvent.OnSuccess -> {
//                            promptToast.showSuccess(R.string.ds_push_success, intercept = true)
//                            lifecycleScope.launchWhenStarted {
//                                delay(2000)
//                                dismiss()
//                            }
//                        }
//                        is DfuViewModel.DfuEvent.OnFail -> {
//                            promptToast.showDfuFail(requireContext(), it.error)
//                        }
//                    }
//                }
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(viewBind.root)
            .setCancelable(true)
            .create()
    }

    private fun resetStateView() {
//        viewBind.stateView.isEnabled = adapter?.hasSelectedItem() ?: true
//        viewBind.stateView.text = getString(R.string.ds_push_start, fileSizeStr(dialPacket.binSize))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBind = null
    }

}