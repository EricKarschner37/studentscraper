package com.eric.karschner.studentscraper

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.support.v7.widget.RecyclerView
import android.view.View
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.service_list_item.view.*
import android.content.Intent
import android.net.Uri


class ServiceAdapter(val items : ArrayList<Service>, val context: Context) : RecyclerView.Adapter<ServiceAdapter.ViewHolder>() {

    var onItemClick: ((Service) -> Unit)? = null
    val backgrounds : ArrayList<String> = ArrayList()

    // Gets the number of services in the list
    override fun getItemCount(): Int {
        return items.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        backgrounds.add("#7395AE")
        backgrounds.add("#B1A296")
        backgrounds.add("#557A95")
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.service_list_item, parent, false))
    }

    // Binds each service in the ArrayList to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Picasso.with(context).load(items.get(position).imageid).into(holder.logoIV)
        holder.nameTV?.text = items.get(position).name
        holder.userTV?.text = items.get(position).user
        holder.assignmentsTV?.text = items.get(position).assignments
        holder.dateTV?.text = items.get(position).date
        holder.item.setBackgroundColor(Color.parseColor(backgrounds[position]))
        holder.item.setOnClickListener{
            val uri = Uri.parse(items.get(position).url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        }
    }

    inner class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val logoIV = view.logo_iv
        val nameTV = view.name_tv
        val userTV = view.user_tv
        val assignmentsTV = view.assignments_tv
        val dateTV = view.date_tv
        val deleteIB = view.delete_IB
        val item = view

        init {
            deleteIB.setOnClickListener{
                onItemClick?.invoke(items[adapterPosition])
            }
        }
    }
}