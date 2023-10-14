package com.sjbt.sdk.sample.ui.device.dial.library

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import com.base.sdk.entity.apps.WmDial
import com.github.kilnn.tool.ui.DisplayUtil
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.AsyncViewModel
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.SingleAsyncState
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.data.device.isConnected
import com.sjbt.sdk.sample.databinding.FragmentDialLibraryBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.model.user.DialMock
import com.sjbt.sdk.sample.ui.device.dial.DialEvent
import com.sjbt.sdk.sample.ui.device.dial.DialInstalledViewModel
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.showFailed
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.sjbt.sdk.sample.widget.GridSpacingItemDecoration
import com.sjbt.sdk.sample.widget.LoadingView
import kotlinx.coroutines.launch

class DialLibraryFragment : BaseFragment(R.layout.fragment_dial_library) {

    private val viewBind: FragmentDialLibraryBinding by viewBinding()

    private val dialInstalledViewModel: DialInstalledViewModel by viewModels()
    private val dialLibraryViewModel: DialLibraryViewModel by viewModels()

    private lateinit var adapter: DialLibraryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = DialLibraryAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBind.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        viewBind.recyclerView.addItemDecoration(
            GridSpacingItemDecoration(
                3,
                DisplayUtil.dip2px(requireContext(), 15F),
                true
            )
        )
        adapter.listener = object : DialLibraryAdapter.Listener {

            override fun onItemClick(packet: DialMock) {
                if (Injector.getDeviceManager().isConnected()) {
                        DialLibraryDfuDialogFragment.newInstance(packet)
                            .show(childFragmentManager, null)
                } else {
                    promptToast.showInfo(R.string.device_state_disconnected)
                }
            }
        }

        viewBind.recyclerView.adapter = adapter
        viewBind.loadingView.listener = LoadingView.Listener {
            dialInstalledViewModel.requestInstallDials()
        }
        viewLifecycle.launchRepeatOnStarted {
            launch {
                dialInstalledViewModel.flowState.collect { state ->
                    when (state.requestDials) {
                        is Loading -> {
                            viewBind.loadingView.showLoading()
                        }
                        is Fail -> {
                            viewBind.loadingView.showError(R.string.tip_load_error)
                        }
                        is Success -> {
                            val wmDials = state.requestDials()
                            if (wmDials.isNullOrEmpty()) {
                                viewBind.loadingView.showError(R.string.ds_no_data)
                            } else {
                                adapter.items = dialLibraryViewModel.refreshInternal(wmDials)
                                adapter.notifyDataSetChanged()
                                viewBind.loadingView.visibility = View.GONE
                            }
                        }
                        else -> {}
                    }
                }
            }
            launch {
                dialInstalledViewModel.flowEvent.collect { event ->
                    when (event) {
                        is DialEvent.RequestFail -> {
                            promptToast.showFailed(event.throwable)
                        }
                        is DialEvent.DialRemoved -> {
                            viewBind.loadingView.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }
}

data class PushParamsAndPackets(
    val packets: List<DialMock>,
) {
    override fun toString(): String {
        return ", packets size:${packets.size}"
    }
}

/**
 * Request and combine [DialPushParams] and [DialPacket] list
 */
class DialLibraryViewModel(
) : AsyncViewModel<SingleAsyncState<PushParamsAndPackets>>(SingleAsyncState()) {

    fun refreshInternal(wmDials: MutableList<WmDial>): MutableList<DialMock> {
        val packets = mutableListOf<DialMock>()
        packets.add(
            DialMock(
                R.mipmap.a8c637a6c26d476db361051786e773df7,
                "8c637a6c26d476db361051786e773df7.dial",installDialsContain(wmDials,"8c637a6c26d476db361051786e773df7")
            )
        )
        packets.add(
            DialMock(
                R.mipmap.a59c4aad46ed434ca58786f3232aba673_california_simple,
                "59c4aad46ed434ca58786f3232aba673.dial",installDialsContain(wmDials,"59c4aad46ed434ca58786f3232aba673")
            )
        )
        packets.add(
            DialMock(
                R.mipmap.a4974f889d52c4a519eac9ea409b3295c,
                "4974f889d52c4a519eac9ea409b3295c.dial",installDialsContain(wmDials,"4974f889d52c4a519eac9ea409b3295c")
            )
        )
        packets.add(
            DialMock(
                R.mipmap.a1245156a62de4d6d8d60d8f8ff751302,
                "1245156a62de4d6d8d60d8f8ff751302.dial",installDialsContain(wmDials,"1245156a62de4d6d8d60d8f8ff751302")
            )
        )
        packets.add(
            DialMock(
                R.mipmap.aaab168c15c7b40eab361ca98fdd213ee,
                "aab168c15c7b40eab361ca98fdd213ee.dial",installDialsContain(wmDials,"aab168c15c7b40eab361ca98fdd213ee")
            )
        )
        return packets
    }

    private fun installDialsContain(wmDials: MutableList<WmDial>, s: String): Int {
        for (bean in wmDials) {
            if (s.contains(bean.id)) {
                return 1
            }
        }
        return 0

    }

}