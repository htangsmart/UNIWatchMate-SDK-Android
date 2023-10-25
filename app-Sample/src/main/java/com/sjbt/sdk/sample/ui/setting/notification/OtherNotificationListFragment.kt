package com.sjbt.sdk.sample.ui.setting.notification

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.databinding.FragmentDialInstalledListBinding
import com.sjbt.sdk.sample.databinding.FragmentOtherNotificationListBinding
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.showFailed
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.sjbt.sdk.sample.widget.LoadingView
import kotlinx.coroutines.launch

class OtherNotificationListFragment : BaseFragment(R.layout.fragment_other_notification_list) {

    private val viewBind: FragmentOtherNotificationListBinding by viewBinding()
    private val viewModel: OtherNotificationViewModel by viewModels()
    private lateinit var adapter: OtherNotificationAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBind.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewBind.recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        adapter = OtherNotificationAdapter()
        adapter.listener = object : OtherNotificationAdapter.Listener {

            override fun onItemModify(position: Int, check: Boolean) {
                if (adapter.sources?.get(position)?.status != 1) {
                    promptProgress.showProgress(getString(R.string.action_deling))
                } else {
                    promptToast.showFailed(getString(R.string.tip_inner_dial_del_error))
                }
            }
        }
        adapter.registerAdapterDataObserver(adapterDataObserver)
        viewBind.recyclerView.adapter = adapter

        viewBind.loadingView.listener = LoadingView.Listener {
            viewModel.requestInstallDials()
        }
        viewBind.loadingView.associateViews = arrayOf(viewBind.recyclerView)


        viewLifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.collect { state ->
                    when (state.requestDials) {
                        is Loading -> {
                            viewBind.loadingView.showLoading()
                        }

                        is Fail -> {
                            viewBind.loadingView.showError(R.string.tip_load_error)
                        }

                        is Success -> {
                            val alarms = state.requestDials()
                            if (alarms == null || alarms.isEmpty()) {
                                viewBind.loadingView.showError(R.string.ds_no_data)
                            } else {
                                viewBind.loadingView.visibility = View.GONE
                            }
                            adapter.sources = alarms
                            adapter.notifyDataSetChanged()
                        }

                        else -> {}
                    }
                }
            }
            launch {
                viewModel.flowEvent.collect { event ->
                    when (event) {
                        is DialEvent.RequestFail -> {
                            promptToast.showFailed(event.throwable)
                        }

                        is DialEvent.DialRemoved -> {
                            promptProgress.dismiss()
                            viewBind.loadingView.visibility = View.GONE
                            adapter.notifyItemRemoved(event.position)
                        }
                    }
                }
            }
        }
    }

    private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            if (adapter.itemCount <= 0) {
                viewBind.loadingView.showError(R.string.ds_no_data)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.unregisterAdapterDataObserver(adapterDataObserver)
    }

}