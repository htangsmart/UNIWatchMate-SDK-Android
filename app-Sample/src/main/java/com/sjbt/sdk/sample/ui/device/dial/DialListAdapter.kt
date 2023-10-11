package com.sjbt.sdk.sample.ui.device.dial

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.base.sdk.entity.apps.WmDial
import com.sjbt.sdk.sample.databinding.ItemDialInstalledListBinding
import com.topstep.fitcloud.sample2.ui.device.alarm.AlarmHelper

class DialListAdapter() :
    RecyclerView.Adapter<DialListAdapter.ItemViewHolder>() {

    var listener: Listener? = null

    var sources: List<WmDial>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemDialInstalledListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val dial = sources?.get(position) ?: return

        holder.viewBind.tvDialId.text = dial.id
        holder.viewBind.imgDelete.visibility=if(dial.status==1) View.GONE else View.VISIBLE
        holder.viewBind.tvDialBuiltIn.visibility=if(dial.status==1) View.VISIBLE else View.GONE
        holder.viewBind.imgDelete.setOnClickListener {
            listener?.onItemDelete(holder.bindingAdapterPosition)
        }

    }

    override fun getItemCount(): Int {
        return sources?.size ?: 0
    }

    interface Listener {
        fun onItemDelete(position: Int)
    }

    class ItemViewHolder(val viewBind: ItemDialInstalledListBinding) :
        RecyclerView.ViewHolder(viewBind.root)

}