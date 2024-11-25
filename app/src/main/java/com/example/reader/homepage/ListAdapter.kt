package com.example.reader.homepage

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.reader.databinding.ItemOcrBinding
import com.example.reader.features.result_details.Document
import com.example.reader.features.result_details.ResultDetailActivity

class ListAdapter(var items : MutableList<Document>, val context : Context) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemOcrBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemOcrBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.timestampTextView.setText(item.timestamp)
        holder.binding.titleTextView.setText(item.title)
        holder.binding.itemRecyclerView.setOnClickListener {
            var intent = Intent(context, ResultDetailActivity::class.java)
            intent.putExtra("isRecyclerView", true)
            intent.putExtra("id", item.id)
            startActivity(context,intent,null)
        }
    }


}