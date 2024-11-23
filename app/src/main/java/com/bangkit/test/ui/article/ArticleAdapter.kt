package com.bangkit.test.ui.article

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.test.R
import com.bangkit.test.databinding.ItemArticleBinding
import com.bumptech.glide.Glide

class ArticleAdapter(
    private val articles: List<NewsResultsItem>,
    private val onClick: (NewsResultsItem) -> Unit
) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(private val binding: ItemArticleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(article: NewsResultsItem) {
            with(binding) {
                tvTitle.text = article.title
                tvSource.text = article.source.name
                tvDate.text = article.date

                Glide.with(itemView.context)
                    .load(article.thumbnail)
                    .placeholder(R.drawable.ic_place_holder)
                    .into(ivThumbnail)

                root.setOnClickListener { onClick(article) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding =
            ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(articles[position])
    }

    override fun getItemCount(): Int = articles.size
}