package com.fafadiatech.newscout.comments

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.db.NewsEntity
import org.w3c.dom.Comment

class CommentAdapter(context: Context) : PagedListAdapter<CommentList, RecyclerView.ViewHolder>(DIFF_CALLBACK){

    companion object {

        val DIFF_CALLBACK:DiffUtil.ItemCallback<CommentList> = object : DiffUtil.ItemCallback<CommentList>(){
            override fun areItemsTheSame(oldItem: CommentList, newItem: CommentList): Boolean {
                if(oldItem is CommentList && newItem is CommentList){
                    return oldItem.id == newItem.id
                }
                return false
            }

            override fun areContentsTheSame(oldItem: CommentList, newItem: CommentList): Boolean {
                if(oldItem is CommentList && newItem is CommentList){
                    return oldItem.equals(newItem)
                }
                return false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var inflater = LayoutInflater.from(parent.context)
        return CommItemViewHolder(inflater.inflate(R.layout.item_comment, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var comList = currentList
        var comHolder = holder as CommItemViewHolder
        val comment = getItem(position)
        comment?.let {
            comHolder.comUser.text = it.user_name
            comHolder.newsTime.text = it.created_at
            comHolder.commText.text = it.comment
        }
    }


    class CommItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var comUser = view.findViewById<TextView>(R.id.tv_user)
        var commText = view.findViewById<TextView>(R.id.tv_comment)
        var newsTime = view.findViewById<TextView>(R.id.tv_time)
    }

}