package com.example.fittrack.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fittrack.databinding.ItemMealCardBinding
import com.example.fittrack.model.DiaryEntryEntity

class DiaryAdapter(private val entries: List<DiaryEntryEntity>) : 
    RecyclerView.Adapter<DiaryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemMealCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMealCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]
        holder.binding.tvMealName.text = entry.name
        holder.binding.tvMealCalories.text = "${entry.calories} kcal • ${entry.mealType.lowercase().replaceFirstChar { it.uppercase() }}"
        holder.binding.btnAddFood.setIconResource(android.R.drawable.ic_menu_delete) // Reuse button for delete if needed
    }

    override fun getItemCount() = entries.size
}
