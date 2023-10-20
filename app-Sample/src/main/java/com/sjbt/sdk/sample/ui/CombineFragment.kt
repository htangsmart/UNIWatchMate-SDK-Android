package com.sjbt.sdk.sample.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.base.api.UNIWatchMate
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.*
import com.sjbt.sdk.sample.databinding.FragmentCombineBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.sjbt.sdk.sample.utils.showFailed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

/**
 * This page mainly shows the functions that need to be combined with the "FitCloud" device and APP/mobile data
 *
 * <p>
 * 1.User info(id, height, weight, sex, etc.)
 * Almost every APP has an account system, and the "FitCloud" device needs to use a unique id to distinguish different users.
 * ToNote: When using a different user id to bind the device, the device will clear the data of the previous user. This point is very important.
 * When using [FcConnector.connect]to connect to the device, you need to pass in these information.
 * When editing user information, such as changing height, you need to set these changes to the device
 *
 * <p>
 * 2.Women Health Function
 * Due to historical legacy issues, when reading this config from the device, only partial data may be returned.
 * Therefore, it is recommended not to read this config from the device, but always follow the config in your APP.
 *
 * <p>
 * 3. Exercise Goal
 * Most devices can only [FcSettingsFeature.setExerciseGoal] and cannot read them from the device.
 * So this part of the data needs to be stored in the APP, such as the database.
 *
 */
class CombineFragment : BaseFragment(R.layout.fragment_combine) {

    private val viewBind: FragmentCombineBinding by viewBinding()
    private val viewModel by viewModels<CombineViewModel>()
//    private val womenHealthRepository = Injector.getWomenHealthRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.itemExerciseGoal.setOnClickListener {
            findNavController().navigate(CombineFragmentDirections.toExerciseGoal())
        }
        
        viewBind.itemUserInfo.setOnClickListener {
            findNavController().navigate(CombineFragmentDirections.toEditUserInfo())
        }
//        viewBind.itemWomenHealthDetail.clickTrigger {
//            val mode = womenHealthRepository.flowCurrent.value?.mode ?: return@clickTrigger
//            findNavController().navigate(CombineFragmentDirections.toWhDetail(mode))
//        }
//        viewBind.itemExerciseGoal.clickTrigger {
//            findNavController().navigate(CombineFragmentDirections.toExerciseGoal())
//        }
        viewBind.btnSignOut.setOnClickListener {
            viewModel.signOut()
        }
        lifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.collect {
                    if (it.async is Loading) {
                        promptProgress.showProgress(R.string.account_sign_out_ing)
                    } else {
                        promptProgress.dismiss()
                    }
                }
            }
            launch {
                viewModel.flowEvent.collect {
                    when (it) {
                        is AsyncEvent.OnFail -> promptToast.showFailed(it.error)
                        is AsyncEvent.OnSuccess<*> -> {
                            startActivity(Intent(requireContext(), LaunchActivity::class.java))
                            requireActivity().finish()
                        }
                    }
                }
            }
//            launch {
//                womenHealthRepository.flowCurrent.collect {
//                    updateWomenHealth(it)
//                }
//            }
//            launch {
//                viewModel.flowDeviceInfo.collect {
//                    Log.e("Kilnn", "deviceInfo:$it")
//                }
//            }
        }
    }

}

class CombineViewModel : AsyncViewModel<SingleAsyncState<Unit>>(SingleAsyncState()) {

    private val authManager = Injector.getAuthManager()

    fun signOut() {
        suspend {
            //Delay 3 seconds. Simulate the sign out process
            delay(1500)
            authManager.signOut()
            UNIWatchMate.disconnect()
        }.execute(SingleAsyncState<Unit>::async) {
            copy(async = it)
        }
    }
}