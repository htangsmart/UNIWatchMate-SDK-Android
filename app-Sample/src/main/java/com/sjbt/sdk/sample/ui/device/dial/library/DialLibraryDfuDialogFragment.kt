package com.sjbt.sdk.sample.ui.device.dial.library

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.base.api.UNIWatchMate
import com.base.sdk.port.AbWmTransferFile
import com.base.sdk.port.State
import com.base.sdk.port.WmTransferState
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.databinding.DialogDialLibraryDfuBinding
import com.sjbt.sdk.sample.model.user.DialMock
import com.sjbt.sdk.sample.utils.PermissionHelper
import com.sjbt.sdk.sample.utils.getParcelableCompat
import com.sjbt.sdk.sample.utils.glideShowMipmapImage
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.*
import kotlinx.coroutines.delay
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
    private val promptToast by promptToast()

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
            if (!dfuViewModel.isDfuIng()) {
                PermissionHelper.requestBle(this) { granted ->
                    if (granted) {
                        isCancelable = false
                        dfuViewModel.startDfu(dialPacket) {
                            UNIWatchMate.wmLog.logI("DfuViewModel", it.toString())
                            onWmTransferStateChange(it)
                        }
                    }
                }
            }
        }
        lifecycle.launchRepeatOnStarted {
            launch {
                dfuViewModel.flowDfuEvent.collect {
                    when (it) {
                        is DfuViewModel.DfuEvent.OnSuccess -> {
                            promptToast.showSuccess(R.string.ds_push_success, intercept = true)
                            lifecycleScope.launchWhenStarted {
                                delay(2000)
                                dismiss()
                            }
                        }
                        is DfuViewModel.DfuEvent.OnFail -> {
                            promptToast.showDfuFail(requireContext(), it.error)
                        }
                    }
                }
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(viewBind.root)
            .setCancelable(true)
            .create()
    }

    private fun onWmTransferStateChange(it: WmTransferState?) {
        it?.let {
            if (it.state == State.FINISH) {
                viewBind.stateView.progress=100
                viewBind.stateView.text=getString(R.string.ds_push_success)
            }else{
                viewBind.stateView.progress=it.progress
                viewBind.stateView.text=if(it.sendingFile!!.name.contains("jpg")) getString(R.string.send_dial_Cover)
                else getString(R.string.send_dial)
            }
        }
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