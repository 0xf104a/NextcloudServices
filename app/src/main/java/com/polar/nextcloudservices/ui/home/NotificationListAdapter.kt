package com.polar.nextcloudservices.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.polar.nextcloudservices.Database.DatabaseHandler
import com.polar.nextcloudservices.Database.NotificationDatabaseentry
import com.polar.nextcloudservices.NotificationProcessors.BasicNotificationProcessor
import com.polar.nextcloudservices.Util
import com.polar.nextcloudservices.Util.prettifyChannelName
import com.polar.nextcloudservices.databinding.AdapterNotificationListBinding
import org.json.JSONObject
import java.security.AccessController.getContext

class NotificationListAdapter (private var mDataset: ArrayList<NotificationDatabaseentry>, private var mContext: Context) : RecyclerView.Adapter<NotificationListAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = AdapterNotificationListBinding.inflate(layoutInflater, parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(mDataset[position], mContext)
    }

    override fun getItemCount() = mDataset.size

    class NotificationViewHolder(private val binding: AdapterNotificationListBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(entry: NotificationDatabaseentry, context: Context) {
            //binding.executePendingBindings()

            var json = JSONObject(entry.content)
            val app = Util.prettifyChannelName(context, json.getString("app"))
            val subject: String = json.getString("subject")
            val message: String = json.getString("message")
            binding.appName.text = app
            if(message == ""){
                binding.content.text = subject
            } else {
                binding.content.text = message
            }

            if (json.has("link")) {
                binding.row.setOnClickListener {
                    val link = json.getString("link")
                    var intent = Intent(Intent.ACTION_VIEW)
                    intent = intent.setData(Uri.parse(link))
                    context.startActivity(intent)

                }
            }
        }
    }

}


