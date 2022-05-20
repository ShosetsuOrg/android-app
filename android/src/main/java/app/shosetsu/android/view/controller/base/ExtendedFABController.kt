package app.shosetsu.android.view.controller.base

import android.util.Log
import androidx.annotation.CallSuper
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.recyclerview.widget.RecyclerView
import app.shosetsu.android.common.ext.logID
import app.shosetsu.android.common.ext.percentageScrolled
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.launch

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
 * 03 / 06 / 2020
 *
 * For views with an FAB, to provide proper transition support
 */
interface ExtendedFABController {
	/**
	 * Hide the FAB
	 */
	fun hideFAB(fab: ExtendedFloatingActionButton) {
		Log.d(logID(), "Hiding FAB")
		fab.hide()
	}

	/**
	 * Show the FAB
	 */
	fun showFAB(fab: ExtendedFloatingActionButton) {
		Log.d(logID(), "Showing FAB")
		fab.show()
	}

	/**
	 * Reset the fab to its original state
	 */
	@CallSuper
	fun resetFAB(fab: ExtendedFloatingActionButton) {
		Log.d(logID(), "Resetting FAB listeners")
		fab.setOnClickListener(null)
		manipulateFAB(fab)
	}

	/**
	 * Change FAB for your use case
	 */
	fun manipulateFAB(fab: ExtendedFloatingActionButton)
}


/**
 * Syncs the FAB with the recyclerview, hiding it when scrolling and showing again when idle
 */
fun ExtendedFABController.syncFABWithRecyclerView(
	recyclerView: RecyclerView,
	fab: ExtendedFloatingActionButton
) =
	recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
		override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
			when (newState) {
				RecyclerView.SCROLL_STATE_DRAGGING -> hideFAB(fab)
				RecyclerView.SCROLL_STATE_IDLE -> {
					if (recyclerView.percentageScrolled() < (2)) {
						showFAB(fab)
						fab.shrink()
					} else {
						showFAB(fab)
						fab.extend()
					}
				}
			}
		}
	})

@Composable
fun syncFABWithCompose(
	state: LazyListState,
	fab: ExtendedFloatingActionButton
) {
	LaunchedEffect(state.isScrollInProgress) {
		launch {
			if (state.isScrollInProgress) {
				fab.hide()
			} else {
				fab.show()
				if (state.firstVisibleItemIndex > 1)
					fab.shrink()
				else fab.extend()
			}
		}
	}
}

@Composable
fun syncFABWithCompose(
	state: LazyGridState,
	fab: ExtendedFloatingActionButton
) {
	LaunchedEffect(state.isScrollInProgress) {
		launch {
			if (state.isScrollInProgress) {
				fab.hide()
			} else {
				fab.show()
				if (state.firstVisibleItemIndex > 1)
					fab.shrink()
				else fab.extend()
			}
		}
	}
}

