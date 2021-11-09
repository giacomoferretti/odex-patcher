/*
 * Copyright 2020-2021 Giacomo Ferretti
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
