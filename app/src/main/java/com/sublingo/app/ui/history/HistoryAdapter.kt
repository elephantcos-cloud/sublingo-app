package com.sublingo.app.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sublingo.app.data.db.TranslationEntity
import com.sublingo.app.databinding.ItemHistoryBinding
import com.sublingo.app.translator.MLKitTranslator
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val onShare: (TranslationEntity) -> Unit,
    private val onDelete: (TranslationEntity) -> Unit
) : ListAdapter<TranslationEntity, HistoryAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<TranslationEntity>() {
            override fun areItemsTheSame(a: TranslationEntity, b: TranslationEntity) = a.id == b.id
            override fun areContentsTheSame(a: TranslationEntity, b: TranslationEntity) = a == b
        }
    }

    inner class VH(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val entity = getItem(position)
        val b = holder.binding

        b.tvFileName.text = entity.fileName
        val srcName = MLKitTranslator.LANGUAGES.find { it.first == entity.sourceLang }?.second ?: entity.sourceLang
        val tgtName = MLKitTranslator.LANGUAGES.find { it.first == entity.targetLang }?.second ?: entity.targetLang
        b.tvLangPair.text = "$srcName → $tgtName"
        b.tvLineCount.text = "${entity.lineCount} lines"
        val fmt = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        b.tvDate.text = fmt.format(Date(entity.createdAt))

        b.btnShare.setOnClickListener { onShare(entity) }
        b.btnDelete.setOnClickListener { onDelete(entity) }
    }
}
