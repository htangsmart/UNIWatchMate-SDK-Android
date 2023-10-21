package com.sjbt.sdk.sample.ui.device.sport

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.base.sdk.entity.apps.WmSport
import com.sjbt.sdk.sample.databinding.ItemSportInstalledListBinding

class SportListAdapter() :
    RecyclerView.Adapter<SportListAdapter.ItemViewHolder>() {

    var listener: Listener? = null

    var sources: List<WmSport>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemSportInstalledListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val sport = sources?.get(position) ?: return

        holder.viewBind.tvDialId.text = sport.id.toString()
        holder.viewBind.imgDelete.visibility=if(sport.type==1) View.GONE else View.VISIBLE
        holder.viewBind.tvDialBuiltIn.visibility=if(sport.type==1) View.VISIBLE else View.GONE
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

    class ItemViewHolder(val viewBind: ItemSportInstalledListBinding) :
        RecyclerView.ViewHolder(viewBind.root)

}