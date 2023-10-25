package com.sjbt.sdk.sample.ui.setting.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.base.sdk.entity.apps.WmDial
import com.sjbt.sdk.sample.databinding.ItemDialInstalledListBinding
import com.sjbt.sdk.sample.databinding.ItemOtherNotificationBinding

class OtherNotificationAdapter() :
    RecyclerView.Adapter<OtherNotificationAdapter.ItemViewHolder>() {

    var listener: Listener? = null

    var sources: List<WmDial>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemOtherNotificationBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val dial = sources?.get(position) ?: return

        holder.viewBind.itemOtherNotification.getTitleView().text = dial.id
        holder.viewBind.itemOtherNotification.getSwitchView()
            .setOnCheckedChangeListener { buttonView, isChecked ->
                listener?.onItemModify(position, isChecked)
            }
    }

    override fun getItemCount(): Int {
        return sources?.size ?: 0
    }

    interface Listener {
        fun onItemModify(position: Int, check: Boolean)
    }

    class ItemViewHolder(val viewBind: ItemOtherNotificationBinding) :
        RecyclerView.ViewHolder(viewBind.root)

}