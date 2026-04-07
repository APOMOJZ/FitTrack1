package com.example.fittrack.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fittrack.databinding.ItemFoodRowBinding
import com.example.fittrack.model.FoodEntry

class FoodAdapter(
    private val items: List<FoodEntry>,
    private val onItemClick: (FoodEntry) -> Unit
) : RecyclerView.Adapter<FoodAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemFoodRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFoodRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvFoodName.text = item.name
        holder.binding.tvBrandAndServing.text = "${item.brand}, ${item.servingSize}"
        holder.binding.tvCalories.text = item.calories.toString()
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
}