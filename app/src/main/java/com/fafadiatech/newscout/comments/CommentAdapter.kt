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
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import org.w3c.dom.Comment
import java.text.SimpleDateFormat
import java.util.*

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

            comHolder.commText.text = it.comment

            var dateString: String = it.created_at
            if (dateString != null) {
                var timeAgo: String = ""
                try {
                    if (dateString.endsWith("Z", false) == false) {
                        dateString += "Z"
                    }

                    var timeZone = Calendar.getInstance().timeZone.id
                    var dateformat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    dateformat.timeZone = TimeZone.getTimeZone("UTC")
                    var date = dateformat.parse(dateString)

                    dateformat = SimpleDateFormat("dd-MM-yyyy")
                    dateformat.timeZone = TimeZone.getTimeZone(timeZone)

                    val strDate = dateformat.format(date)
                    comHolder.newsTime.text = strDate
                } catch (e: Exception) {

                    try {
                        var timeZone = Calendar.getInstance().timeZone.id
                        var dateformat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
                        dateformat.timeZone = TimeZone.getTimeZone("UTC")
                        var date = dateformat.parse(dateString)

                        dateformat = SimpleDateFormat("dd-MM-yyyy")
                        dateformat.timeZone = TimeZone.getTimeZone(timeZone)
                        val strDate = dateformat.format(date)
                        comHolder.newsTime.text = strDate
                    } catch (exception: Exception) {
                        comHolder.newsTime.text = ""
                    }
                }
            } else {
                comHolder.newsTime.text = ""
            }

        }
    }


    class CommItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var comUser = view.findViewById<TextView>(R.id.tv_user)
        var commText = view.findViewById<TextView>(R.id.tv_comment)
        var newsTime = view.findViewById<TextView>(R.id.tv_time)
    }

}