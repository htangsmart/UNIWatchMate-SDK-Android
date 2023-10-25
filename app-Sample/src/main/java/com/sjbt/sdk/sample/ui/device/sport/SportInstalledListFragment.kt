package com.sjbt.sdk.sample.ui.device.sport

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.databinding.FragmentSportInstalledListBinding
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.showFailed
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.sjbt.sdk.sample.widget.LoadingView
import kotlinx.coroutines.launch

class SportInstalledListFragment : BaseFragment(R.layout.fragment_sport_installed_list) {

    private val viewBind: FragmentSportInstalledListBinding by viewBinding()
    private val viewModel: SportInstalledViewModel by viewModels()
    private lateinit var adapter: SportListAdapter
//    private val quickDragAndSwipe = QuickDragAndSwipe()
//        .setDragMoveFlags(
//            ItemTouchHelper.UP or ItemTouchHelper.DOWN or
//                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
//        )
//        .setSwipeMoveFlags(ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//      (requireActivity() as AppCompatActivity?)?.supportActionBar?.setTitle(R.string.ds_dial_installed)

        viewBind.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewBind.recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        adapter = SportListAdapter()
        adapter.listener = object : SportListAdapter.Listener {

            override fun onItemDelete(position: Int) {
                if (adapter.sources?.get(position)?.type != 1) {
                    promptProgress.showProgress(getString(R.string.action_deling))
                    viewModel.deleteAlarm(position)
                } else {
                    promptToast.showFailed(getString(R.string.tip_inner_sport_del_error))
                }
            }
        }
        adapter.registerAdapterDataObserver(adapterDataObserver)
        viewBind.recyclerView.adapter = adapter

        viewBind.loadingView.listener = LoadingView.Listener {
            viewModel.requestInstallSports()
        }
        viewBind.loadingView.associateViews = arrayOf(viewBind.recyclerView)


        viewLifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.collect { state ->
                    when (state.requestSports) {
                        is Loading -> {
                            viewBind.loadingView.showLoading()
                        }

                        is Fail -> {
                            viewBind.loadingView.showError(R.string.tip_load_error)
                        }

                        is Success -> {
                            val alarms = state.requestSports()
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
                        is SportEvent.RequestFail -> {
                            promptToast.showFailed(event.throwable)
                        }

                        is SportEvent.DialRemoved -> {
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