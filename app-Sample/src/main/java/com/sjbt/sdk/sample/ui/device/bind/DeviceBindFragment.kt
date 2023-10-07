package com.sjbt.sdk.sample.ui.device.bind

import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cn.bertsir.zbar.Qr.ScanResult
import com.base.api.UNIWatchMate
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.entity.WmScanDevice
import com.base.sdk.port.AbWmConnect
import com.github.kilnn.tool.dialog.prompt.PromptDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.data.device.DeviceManager
import com.sjbt.sdk.sample.databinding.FragmentDeviceBindBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.ui.bind.DeviceConnectDialogFragment
import com.sjbt.sdk.sample.utils.*
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.sjbt.sdk.sample.widget.CustomDividerItemDecoration
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import timber.log.Timber

/**
 * Scan and bind device.
 */
class DeviceBindFragment : BaseFragment(R.layout.fragment_device_bind),
    PromptDialogFragment.OnPromptListener, DeviceConnectDialogFragment.Listener {
    private /*const*/ val promptBindSuccessId = 1

    private val viewBind: FragmentDeviceBindBinding by viewBinding()
    /**
     * Avoid repeated requests for permissions at the same time
     */
    private var isRequestingPermission: Boolean = false

    private val deviceManager: DeviceManager = Injector.getDeviceManager()

    private val otherDevicesAdapter: OtherDevicesAdapter = OtherDevicesAdapter().apply {
        listener = object : OtherDevicesAdapter.Listener {
            override fun onItemClick(device: WmScanDevice) {
                tryingBind(device.address ?: "", device.name)
            }
        }
    }

    private val scanDevicesAdapter: ScanDevicesAdapter = ScanDevicesAdapter().apply {
        listener = object : ScanDevicesAdapter.Listener {
            override fun onItemClick(device: ScanDevice) {
                tryingBind(device.address, device.name)
            }

            override fun onItemSizeChanged(oldSize: Int, newSize: Int) {
                val animator = viewBind.layoutTips.animate()
                if (oldSize == 0 && newSize > 0) {
                    animator.cancel()
                    animator.setDuration(3000).alpha(0.1f).start()
                } else if (oldSize > 0 && newSize == 0) {
                    animator.cancel()
                    animator.setDuration(500).alpha(0.5f).start()
                }
            }
        }
    }

    private var bluetoothSnackbar: Snackbar? = null

    private fun tryingBind(address: String, name: String?) {
        this::class.simpleName?.let { Timber.tag(it).i("address=$address name=$name") }
        val userInfo = AbWmConnect.UserInfo("", "")
        deviceManager.bind(
            address, if (name.isNullOrEmpty()) {
                UNKNOWN_DEVICE_NAME
            } else {
                name
            }
        )
        UNIWatchMate.wmConnect.connect(address,
            AbWmConnect.BindInfo(AbWmConnect.BindType.SCAN_QR, userInfo),
            WmDeviceModel.SJ_WATCH)
        DeviceConnectDialogFragment().show(childFragmentManager, null)
    }

    private fun tryingBind(scanResult: ScanResult) {
        this::class.simpleName?.let { Timber.tag(it).i("scanResult=$scanResult") }
        val userInfo = AbWmConnect.UserInfo("", "")
        val wmScanDevice = UNIWatchMate.mInstance?.parseScanQr(scanResult.getContent())
        wmScanDevice?.let {
            if (wmScanDevice.address != null) {
                deviceManager.bind(
                    wmScanDevice.address!!, if (wmScanDevice.name.isNullOrEmpty()) {
                        UNKNOWN_DEVICE_NAME
                    } else {
                        wmScanDevice.name!!
                    }
                )
                UNIWatchMate.scanQr(scanResult.getContent(),
                    AbWmConnect.BindInfo(AbWmConnect.BindType.SCAN_QR, userInfo))
            }
        }
        DeviceConnectDialogFragment().show(childFragmentManager, null)
    }

    override fun onPromptCancel(promptId: Int, cancelReason: Int, tag: String?) {
        if (promptId == promptBindSuccessId) {
            findNavController().popBackStack()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenResumed {
            if (!isRequestingPermission) {
                isRequestingPermission = true
                PermissionHelper.requestBle(this@DeviceBindFragment) {
                    isRequestingPermission = false
                }
            }
        }
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_device_bind, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.menu_qr_code_scanner) {
                    PermissionHelper.requestAppCamera(this@DeviceBindFragment) {
                        findNavController().navigate(DeviceBindFragmentDirections.toCustomQr())
                    }
                    return true
                }
                return false
            }
        }, viewLifecycleOwner)

//        viewLifecycle.addObserver(scannerHelper)
//
        viewBind.refreshLayout.setOnRefreshListener {
            //Clear data when using pull to refresh. This is a different strategy than fabScan click event
            scanDevicesAdapter.clearItems()
//            if (!scannerHelper.start()) {
//                viewBind.refreshLayout.isRefreshing = false
//            }
            startDiscover()
        }

        viewBind.scanDevicesRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        viewBind.scanDevicesRecyclerView.addItemDecoration(CustomDividerItemDecoration(context,
            LinearLayoutManager.VERTICAL))
        viewBind.scanDevicesRecyclerView.adapter = scanDevicesAdapter

        viewBind.otherDevicesRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        viewBind.otherDevicesRecyclerView.adapter = otherDevicesAdapter

        viewBind.bottomSheetLayout.getBottomSheetBehavior()
            .addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    //refresh bonded/connected devices if bottomSheet expanded and request permission if not granted
                    if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        refreshOtherDevices(false)
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    //do nothing
                }
            })

        viewBind.fabScan.setOnClickListener {
           startDiscover()
        }

        viewLifecycle.launchRepeatOnStarted {
            launch {
//                deviceManager.flowConnectorState.collect {
////                    if (it == ConnectorState.CONNECTED) {
////                        /**
////                         * Show bind success, and exit in [onPromptCancel]
////                         */
////                        promptToast.showSuccess(R.string.device_bind_success, intercept = true, promptId = promptBindSuccessId)
////                    }
//                }
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                launch {
                    flowLocationServiceState(requireContext()).collect { isEnabled ->
                        viewBind.layoutLocationService.isVisible = !isEnabled
                        viewBind.recyclerDivider.isVisible = !isEnabled
                    }
                }
            } else {
                viewBind.layoutLocationService.isVisible = false
            }
        }
        viewBind.btnEnableLocationService.setOnClickListener {
            try {
                requireContext().startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } catch (e: Exception) {
                Timber.w(e)
            }
        }
        setFragmentResultListener(DEVICE_QR_CODE) { requestKey, bundle ->
            if (requestKey == DEVICE_QR_CODE) {
                val scanResult: ScanResult = bundle.getSerializable(EXTRA_SCAN_RESULT) as ScanResult
                scanResult?.let {
                    if (!it.getContent().isNullOrEmpty()) {
                        tryingBind(it)
                    }
                }
            }
        }
    }

    private fun startDiscover() {
        viewLifecycle.launchRepeatOnStarted {
            launch {
                UNIWatchMate.mInstance?.startDiscovery()?.asFlow()?.collect {
                    this::class.simpleName?.let { it1 -> Timber.tag(it1).i(it.toString()) }
                    scanDevicesAdapter.newScanResult(it)
                }
            }
        }
    }

    private fun refreshOtherDevices(checkPermission: Boolean) {
        if (checkPermission && !PermissionHelper.hasBlue(requireContext())) {
            return
        }
        PermissionHelper.requestBle(this) { granted ->
            if (granted) {
//                otherDevicesAdapter.bonded =
//                    OtherDevicesAdapter.devices(bluetoothManager.adapter.bondedDevices)
//                otherDevicesAdapter.connected =
//                    OtherDevicesAdapter.devices(bluetoothManager.getConnectedDevices(
//                        BluetoothProfile.GATT_SERVER))
//                otherDevicesAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun toggleBluetoothAlert(show: Boolean) {
        if (show) {
            val snackbar =
                bluetoothSnackbar ?: createBluetoothSnackbar().also { bluetoothSnackbar = it }
            if (!snackbar.isShownOrQueued) {
                snackbar.show()
            }
        } else {
            bluetoothSnackbar?.dismiss()
        }
    }

    private fun createBluetoothSnackbar(): Snackbar {
        val snackbar = Snackbar.make(viewBind.root,
            R.string.device_state_bt_disabled,
            Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction(R.string.action_turn_on) {
            PermissionHelper.requestBle(this) { granted ->
                if (granted) {
//                    requireContext().startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                }
            }
        }
        return snackbar
    }

    override fun onStop() {
        super.onStop()
        bluetoothSnackbar?.dismiss()
    }

    private fun View.getBottomSheetBehavior(): BottomSheetBehavior<*> {
        return if (this is CoordinatorLayout.AttachedBehavior) {
            this.behavior as BottomSheetBehavior
        } else {
            (this.layoutParams as CoordinatorLayout.LayoutParams).behavior as BottomSheetBehavior
        }
    }

    override fun navToConnectHelp() {
//                findNavController().navigate(DeviceBindFragmentDirections.toConnectHelp())

    }

    override fun navToBgRunSettings() {
        findNavController().navigate(DeviceBindFragmentDirections.toBgRunSettings())
    }

    companion object {
        const val DEVICE_QR_CODE = "device_qr_code"
        const val EXTRA_ADDRESS = "address"
        const val EXTRA_SCAN_RESULT = "scan_result"
        const val EXTRA_NAME = "name"
        const val UNKNOWN_DEVICE_NAME = "Unknown"
    }

}

class ScanErrorDelayDialogFragment : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.device_scan_tips_error)
            .setMessage(R.string.device_scan_tips_delay)
            .setPositiveButton(R.string.tip_i_know, null)
            .create()
    }
}

class ScanErrorRestartDialogFragment : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.device_scan_tips_error)
            .setMessage(R.string.device_scan_tips_restart)
            .setPositiveButton(R.string.tip_i_know, null)
            .create()
    }
}