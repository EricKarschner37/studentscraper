package com.eric.karschner.studentscraper

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.add_service_list_item.view.*

class AddServiceAdapter(var selections: ArrayList<ServiceSelection>, val context : Context) : RecyclerView.Adapter<AddServiceAdapter.ViewHolder>(){

    var onItemClick: ((ServiceSelection) -> Unit)? = null

    override fun getItemCount(): Int {
        return selections.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.add_service_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val service = selections[position]
        holder.addServiceNameTV.text = service.name
        Picasso.with(context).load(service.imageurl).into(holder.addServiceIV)
    }

    inner class ViewHolder(item: View) : RecyclerView.ViewHolder(item){
        val addServiceIV = item.add_service_iv
        val addServiceNameTV = item.add_service_name_tv

        init {
            item.setOnClickListener {
                onItemClick?.invoke(selections[adapterPosition])
            }
        }
    }
}