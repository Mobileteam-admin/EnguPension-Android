package com.enugu.pension.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.enugu.pension.R
import com.enugu.pension.databinding.ItemDocumentUploadBinding
import com.enugu.pension.model.dto.DocumentUploadItem

class DocumentUploadAdapter(public val items: MutableList<DocumentUploadItem?>,
                            private val onRemoveDoc:(item:DocumentUploadItem)->Unit,
                            private val onViewDoc:(item:DocumentUploadItem)->Unit,
                            private val onUploadClick:(item:DocumentUploadItem)->Unit,
    ) :
    RecyclerView.Adapter<DocumentUploadAdapter.ItemViewHolder>() {

    class ItemViewHolder(val binding: ItemDocumentUploadBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =
            ItemDocumentUploadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]!!
        holder.binding.tvTitle.setCompoundDrawablesWithIntrinsicBounds(
            0, 0,
            if (item.isMandatory) R.drawable.asterisk_8 else 0, 0
        )
        holder.binding.tvTitle.text = item.title
        val hasDocumentAdded = item.hasDocumentAdded()
        holder.binding.cvUpload.isGone = !hasDocumentAdded
        holder.binding.btView.isVisible = hasDocumentAdded && !item.isUploading
        holder.binding.btClose.isVisible = hasDocumentAdded && !item.isUploading
        holder.binding.llUploadProgress.isGone = !item.isUploading
        holder.binding.progressBar.progress = item.progress
        holder.binding.tvProgress.text = "${item.progress}%"
//        holder.binding.llPercentage.isGone = !item.isUploading
//        holder.binding.progressBar.isGone = !item.isUploading
        holder.binding.tvFileName.text = item.fileName

        holder.binding.btClose.setOnClickListener {
//            holder.binding.cvUpload.isGone = true

            onRemoveDoc(item)
        }
        holder.binding.btView.setOnClickListener {
            onViewDoc(item)
        }
        holder.binding.llUpload.setOnClickListener {
            onUploadClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

}
