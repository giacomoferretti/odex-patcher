package me.hexile.odexpatcher.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.hexile.odexpatcher.databinding.ListItemAppBinding
import me.hexile.odexpatcher.data.AppInfo

class AppInfoAdapter(private val onClick: (AppInfo) -> Unit) :
    ListAdapter<AppInfo, AppInfoAdapter.ViewHolder>(AppInfoDiffCallback) {

    class ViewHolder(private val binding: ListItemAppBinding, val onClick: (AppInfo) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {
        private var currentApp: AppInfo? = null

        init {
            binding.root.setOnClickListener {
                currentApp?.let {
                    onClick(it)
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(app: AppInfo) {
            currentApp = app

            binding.appIcon.setImageDrawable(app.icon)
            binding.appName.text = app.name
            binding.appVersion.text = "${app.versionName} (${app.versionCode})"
            binding.appPackage.text = app.packageName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

object AppInfoDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
    private fun isSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
        return oldItem.packageName == newItem.packageName && oldItem.versionCode == newItem.versionCode
    }

    override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
        return isSame(oldItem, newItem)
    }

    override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
        return isSame(oldItem, newItem)
    }
}
