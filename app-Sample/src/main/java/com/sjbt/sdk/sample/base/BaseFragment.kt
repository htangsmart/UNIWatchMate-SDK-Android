package com.sjbt.sdk.sample.base

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.sjbt.sdk.sample.utils.promptToast
import com.sjbt.sdk.sample.utils.promptProgress

abstract class BaseFragment : Fragment {

    constructor() : super()

    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    protected val promptToast by promptToast()
    protected val promptProgress by promptProgress()

}