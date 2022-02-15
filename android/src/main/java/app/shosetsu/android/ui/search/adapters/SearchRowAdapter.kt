package app.shosetsu.android.ui.search.adapters

import android.util.Log
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import app.shosetsu.android.common.consts.BundleKeys.BUNDLE_NOVEL_ID
import app.shosetsu.android.common.ext.collectLA
import app.shosetsu.android.common.ext.logID
import app.shosetsu.android.common.ext.logWTF
import app.shosetsu.android.common.ext.setOnClickListener
import app.shosetsu.android.ui.novel.NovelController
import app.shosetsu.android.ui.search.SearchController
import app.shosetsu.android.view.uimodels.model.catlog.ACatalogNovelUI
import app.shosetsu.android.view.uimodels.model.search.SearchRowUI
import app.shosetsu.android.viewmodel.abstracted.ASearchViewModel
import com.bluelinelabs.conductor.Controller
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil

/*
 * This file is part of shosetsu.
 *
 * shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * shosetsu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with shosetsu.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * shosetsu
 * 09 / 09 / 2020
 */
class SearchRowAdapter(
	private val controller: SearchController,
	private val pushController: (Controller) -> Unit,
	private val viewModel: ASearchViewModel
) : FastAdapter<SearchRowUI>() {
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		super.onBindViewHolder(holder, position)
		if (holder !is SearchRowUI.ViewHolder) return
		val binding = holder.binding

		val itemAdapter = ItemAdapter<ACatalogNovelUI>()
		val fastAdapter = object : FastAdapter<ACatalogNovelUI>() {
			override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
				super.onBindViewHolder(holder, position)
				holder.itemView.layoutParams = ViewGroup.MarginLayoutParams(400, 600).apply {
					this.setMargins(10, 10, 10, 10)
				}
			}
		}

		fastAdapter.addAdapter(0, itemAdapter)
		fastAdapter.setOnClickListener { _, _, item, _ ->
			Log.d(logID(), "Pushing")
			pushController(NovelController(bundleOf(BUNDLE_NOVEL_ID to item.id)))
			true
		}

		binding.recyclerView.adapter = fastAdapter

		val handleUpdate = { result: List<ACatalogNovelUI> ->
			binding.progressBar.isVisible = false
			FastAdapterDiffUtil[itemAdapter] = FastAdapterDiffUtil.calculateDiff(
				itemAdapter,
				result
			)
		}

		getItem(position)?.let { (id) ->
			viewModel.getIsLoading(id).collectLA(controller, catch = {
				logWTF("What?")
			}) {
				binding.progressBar.isVisible = it
			}
			if (id != -1) {
				viewModel.searchExtension(id).collectLA(controller,
					catch = {
						TODO("Handle")
					}) {
					handleUpdate(it)
				}
			} else {
				viewModel.searchLibrary().collectLA(controller, catch = { TODO("Handle") }) {
					handleUpdate(it)
				}
			}
		}
	}
}