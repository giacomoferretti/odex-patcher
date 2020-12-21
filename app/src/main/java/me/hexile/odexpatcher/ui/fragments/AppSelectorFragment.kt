/*
 * Copyright 2020 Giacomo Ferretti
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

package me.hexile.odexpatcher.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import me.hexile.odexpatcher.R
import me.hexile.odexpatcher.databinding.FragmentAppSelectorBinding
import me.hexile.odexpatcher.data.AppInfo
import me.hexile.odexpatcher.adapters.AppInfoAdapter
import me.hexile.odexpatcher.core.BaseFragment
import me.hexile.odexpatcher.utils.showSnackbar
import me.hexile.odexpatcher.views.MarginItemDecoration
import me.hexile.odexpatcher.viewmodels.MainActivityViewModel

class AppSelectorFragment : BaseFragment() {
    companion object {
        const val TAG = "AppSelectorFragment"
    }

    private lateinit var binding: FragmentAppSelectorBinding

    private val model by activityViewModels<MainActivityViewModel>()

    override fun onStart() {
        super.onStart()
        activity.title = getString(R.string.choose_an_app)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAppSelectorBinding.inflate(inflater, container, false)

        val appInfoAdapter = AppInfoAdapter { app -> adapterOnClick(app) }

        binding.recyclerView.adapter = appInfoAdapter
        binding.recyclerView.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.fab_margin).toInt()
            )
        )

        model.installedApps.observe(viewLifecycleOwner, { apps ->
            // TODO: Better empty handling? Without using a TextView
            if (apps.isEmpty()) {
                binding.emptyTextView.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyTextView.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                appInfoAdapter.submitList(apps)
            }
        })

        return binding.root
    }

    private fun adapterOnClick(app: AppInfo) {
        model.targetPackage.value = app.packageName

        activity.supportFragmentManager.popBackStack();
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.app_select_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refreshAction -> {
                binding.root.showSnackbar(getString(R.string.reloading_apps), Snackbar.LENGTH_SHORT)
                model.viewModelScope.launch {
                    model.refreshInstalledApps()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}