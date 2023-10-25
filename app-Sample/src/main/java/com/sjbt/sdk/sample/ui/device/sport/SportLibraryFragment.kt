package com.sjbt.sdk.sample.ui.device.sport

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmSport
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ResourceUtils
import com.github.kilnn.tool.ui.DisplayUtil
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.Async
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.StateEventViewModel
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.base.Uninitialized
import com.sjbt.sdk.sample.data.device.isConnected
import com.sjbt.sdk.sample.databinding.FragmentDialLibraryBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.model.LocalSportLibrary
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.showFailed
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.runCatchingWithLog
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.sjbt.sdk.sample.widget.GridSpacingItemDecoration
import com.sjbt.sdk.sample.widget.LoadingView
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await

class SportLibraryFragment : BaseFragment(R.layout.fragment_dial_library) {

    private val viewBind: FragmentDialLibraryBinding by viewBinding()
    private val sportLibraryViewModel: SportLibraryViewModel by viewModels()
    private val sportInstalledViewModel: SportInstalledViewModel by viewModels()
    private var wmSports: MutableList<LocalSportLibrary.LocalSport>? = mutableListOf()
    private var wmIntalledSports: MutableList<WmSport>? = mutableListOf()
    private lateinit var adapter: SportlLibraryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = SportlLibraryAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBind.recyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
        viewBind.recyclerView.addItemDecoration(
            GridSpacingItemDecoration(
                1,
                DisplayUtil.dip2px(requireContext(), 15F),
                true
            )
        )

        adapter.listener = object : SportlLibraryAdapter.Listener {
            override fun onItemClick(packet: LocalSportLibrary.LocalSport, pos: Int) {
                if (Injector.getDeviceManager().isConnected()) {
                    if (packet.buildIn) {
                        promptToast.showInfo(R.string.ds_sport_build_in)
                    } else if (packet.installed) {
                        promptToast.showInfo(R.string.ds_sport_installed)
                    } else {
                        promptProgress.showProgress(getString(R.string.ds_sport_installing))
                        sportLibraryViewModel.installContactContain(pos)
                    }
                } else {
                    promptToast.showInfo(R.string.device_state_disconnected)
                }
            }
        }
        viewBind.recyclerView.adapter = adapter
        viewBind.loadingView.listener = LoadingView.Listener {
            sportInstalledViewModel.requestInstallSports()
        }
        viewLifecycle.launchRepeatOnStarted {
            launch {
                sportInstalledViewModel.flowState.collect { state ->
                    when (state.requestSports) {
                        is Loading -> {
                            viewBind.loadingView.showLoading()
                        }

                        is Fail -> {
                            viewBind.loadingView.showError(R.string.tip_load_error)
                        }

                        is Success -> {
                            wmIntalledSports=state.requestSports()
                            sportLibraryViewModel.requestLibrarySports(state.requestSports())

                        }

                        else -> {

                        }
                    }
                }
            }
            launch {
                sportLibraryViewModel.flowState.collect { state ->
                    when (state.requestSports) {
                        is Loading -> {
                            viewBind.loadingView.showLoading()
                        }

                        is Fail -> {
                            viewBind.loadingView.showError(R.string.tip_load_error)
                        }

                        is Success -> {
                            wmSports = state.requestSports()
                            adapter.items = wmSports
                            adapter.notifyDataSetChanged()
                            viewBind.loadingView.visibility = View.GONE
                        }

                        else -> {

                        }
                    }
                }
            }
            launch {
                sportInstalledViewModel.flowEvent.collect { event ->
                    when (event) {
                        is SportEvent.RequestFail -> {
                            promptToast.showFailed(event.throwable)
                        }
                    }
                }
            }
            launch {
                sportLibraryViewModel.flowEvent.collect { event ->
                    when (event) {
                        is SportLibraryEvent.SportInstallSuccess -> {
                            promptProgress.dismiss()
                            adapter.notifyItemChanged(event.position)
                        }

                        is SportLibraryEvent.SportInstallFail -> {
                            promptProgress.dismiss()
                            ToastUtil.showToast(event.msg)
                        }
                    }
                }
            }
        }
    }
}

data class SportLibraryState(
    val requestSports: Async<MutableList<LocalSportLibrary.LocalSport>> = Uninitialized,
)

sealed class SportLibraryEvent {
    class RequestFail(val throwable: Throwable) : SportLibraryEvent()
    class SportInstallSuccess(val position: Int) : SportLibraryEvent()
    class SportInstallFail(val msg: String) : SportLibraryEvent()
}

/**
 * Request and combine [DialPushParams] and [DialPacket] list
 */
class SportLibraryViewModel(
) : StateEventViewModel<SportLibraryState, SportLibraryEvent>(SportLibraryState()) {

    fun requestLibrarySports(requestSports: MutableList<WmSport>?) {
        viewModelScope.launch {
            state.copy(requestSports = Loading()).newState()
            runCatchingWithLog {
                getWmportLibrarys(requestSports)
//               mutableListOf<WmSport>()
            }.onSuccess {
                if (it is MutableList) {
                    state.copy(requestSports = Success(it)).newState()
                } else {
                    state.copy(requestSports = Fail(Throwable("result is not a mutable list")))
                        .newState()
                }
            }.onFailure {
                state.copy(requestSports = Fail(it)).newState()
                SportLibraryEvent.RequestFail(it).newEvent()
            }
        }
    }

    private fun getWmportLibrarys(requestSports: MutableList<WmSport>?): MutableList<LocalSportLibrary.LocalSport> {
        val sportsData = ResourceUtils.readAssets2String("sports_data.json")
        val localSportLibrary =
            GsonUtils.fromJson<LocalSportLibrary>(sportsData, LocalSportLibrary::class.java)
        requestSports?.let {
            for (wmSport in it) {
                for (localSport in localSportLibrary.sports) {
                    if (wmSport.id == localSport.id) {
                        localSport.buildIn = wmSport.buildIn
                        localSport.installed = true
                    }
                }
            }
        }
        return localSportLibrary.sports
    }

    fun installContactContain(position: Int) {
        viewModelScope.launch {
            val localSport = state.requestSports()?.get(position)
            localSport?.let {
                val wmSport = WmSport(localSport.id, localSport.type, localSport.buildIn)
                runCatchingWithLog {
                    val result = UNIWatchMate.wmApps.appSport.addSport(wmSport).await()
                }.onSuccess {
                    localSport.installed = true
                    SportLibraryEvent.SportInstallSuccess(position)
                }.onFailure {
                    SportLibraryEvent.SportInstallFail(it.toString()).newEvent()
                }
            }
        }
    }

}