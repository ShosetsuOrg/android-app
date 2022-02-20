package app.shosetsu.android.viewmodel.impl

import app.shosetsu.android.common.ext.*
import app.shosetsu.android.domain.usecases.NovelBackgroundAddUseCase
import app.shosetsu.android.domain.usecases.get.GetCatalogueListingDataUseCase
import app.shosetsu.android.domain.usecases.get.GetCatalogueQueryDataUseCase
import app.shosetsu.android.domain.usecases.get.GetExtensionUseCase
import app.shosetsu.android.domain.usecases.load.LoadNovelUIColumnsHUseCase
import app.shosetsu.android.domain.usecases.load.LoadNovelUIColumnsPUseCase
import app.shosetsu.android.domain.usecases.load.LoadNovelUITypeUseCase
import app.shosetsu.android.domain.usecases.settings.SetNovelUITypeUseCase
import app.shosetsu.android.view.uimodels.model.catlog.ACatalogNovelUI
import app.shosetsu.android.view.uimodels.model.catlog.CompactCatalogNovelUI
import app.shosetsu.android.view.uimodels.model.catlog.FullCatalogNovelUI
import app.shosetsu.android.viewmodel.abstracted.ACatalogViewModel
import app.shosetsu.common.consts.settings.SettingKey
import app.shosetsu.common.enums.NovelCardType
import app.shosetsu.lib.Filter
import app.shosetsu.lib.IExtension
import app.shosetsu.lib.PAGE_INDEX
import app.shosetsu.lib.mapify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*

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
 * 01 / 05 / 2020
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModel(
	private val getExtensionUseCase: GetExtensionUseCase,
	private val backgroundAddUseCase: NovelBackgroundAddUseCase,
	private val getCatalogueListingData: GetCatalogueListingDataUseCase,
	private val loadCatalogueQueryDataUseCase: GetCatalogueQueryDataUseCase,
	private val loadNovelUITypeUseCase: LoadNovelUITypeUseCase,
	private val loadNovelUIColumnsHUseCase: LoadNovelUIColumnsHUseCase,
	private val loadNovelUIColumnsPUseCase: LoadNovelUIColumnsPUseCase,
	private val setNovelUIType: SetNovelUITypeUseCase,
) : ACatalogViewModel() {
	private var queryState: String = ""

	/**
	 * Map of filter id to the state to pass into the extension
	 */
	private var filterDataState: HashMap<Int, MutableStateFlow<Any>> = hashMapOf()

	private var ext: IExtension? = null

	private val iExtensionFlow: Flow<IExtension> by lazy {
		extensionIDFlow.transformLatest { extensionID ->
			ext = getExtensionUseCase(extensionID)

			if (ext != null)
				emit(ext!!)
		}
	}

	/**
	 * Flow source for extension ID
	 */
	private val extensionIDFlow: MutableStateFlow<Int> by lazy { MutableStateFlow(-1) }

	override val itemsLive: Flow<List<ACatalogNovelUI>> by lazy {
		itemsFlow.combine(novelCardTypeFlow) { items, type ->
			items.let { list ->
				list.map { card ->
					when (type) {
						NovelCardType.NORMAL -> {
							if (card is FullCatalogNovelUI)
								card
							else FullCatalogNovelUI(
								card.id,
								card.title,
								card.imageURL,
								card.bookmarked
							)
						}
						NovelCardType.COMPRESSED -> {
							if (card is CompactCatalogNovelUI)
								card
							else CompactCatalogNovelUI(
								card.id,
								card.title,
								card.imageURL,
								card.bookmarked
							)
						}
						NovelCardType.COZY -> {
							logE("Cozy type not implemented")
							card
						}
					}
				}
			}
		}.onIO()
	}

	private val itemsFlow: MutableStateFlow<List<ACatalogNovelUI>> by lazy {
		MutableStateFlow(emptyList())
	}

	private val filterItemFlow: Flow<Array<Filter<*>>> by lazy {
		iExtensionFlow.mapLatest { (it.searchFiltersModel) }
	}

	/**
	 * This flow is used to reload the filters
	 */
	private val filterReloadFlow = MutableStateFlow(true)

	override val filterItemsLive: Flow<List<Filter<*>>>
		get() = filterItemFlow.mapLatest { it.toList() }.onIO()

	override val hasSearchLive: Flow<Boolean> by lazy {
		iExtensionFlow.mapLatest { it.hasSearch }.transformLatest {
			_hasSearch = it
			emit(it)
		}.onIO()
	}

	private var _hasSearch: Boolean = false

	override val hasSearch: Boolean
		get() = _hasSearch

	override val extensionName: Flow<String> by lazy {
		iExtensionFlow.mapLatest { it.name }.onIO()
	}

	private var stateManager = StateManager()

	override fun getBaseURL(): Flow<String> =
		flow {
			emitAll(iExtensionFlow.mapLatest { it.baseURL })
		}

	/**
	 * Handles the current state of the UI
	 */
	private inner class StateManager {
		private val loaderManager by lazy { LoaderManager() }

		fun loadMore() {
			loaderManager.loadMore(
				QueryFilter(
					queryState,
					filterDataState.mapValues { it.value.value })
			)
		}

		/**
		 * The idea behind this class is the squeeze all the loading jobs into a single class.
		 * There will only be 1 instance at a time
		 */
		private inner class LoaderManager {
			/**
			 * Current loading job
			 */
			private var loadingJob: Job? = null

			private var _canLoadMore = true

			init {
				itemsFlow.tryEmit(emptyList())
				// TODO LOADING
			}

			/**
			 * The current max page loaded.
			 *
			 * if 2, then the current page that has been appended is 2
			 */
			private var currentMaxPage: Int = 0

			private var values = arrayListOf<ACatalogNovelUI>()

			fun loadMore(queryFilter: QueryFilter) {
				logD("")
				if (_canLoadMore && ((loadingJob != null && (loadingJob!!.isCancelled || loadingJob!!.isCompleted)) || (loadingJob == null))) {
					logD("Proceeding with loading")
					loadingJob = null
					loadingJob = loadData(queryFilter)
				}
			}

			private fun loadData(queryFilter: QueryFilter): Job = launchIO {
				if (ext == null) {
					logE("formatter was null")
					this.cancel("Extension not loaded")
					return@launchIO
				}
				itemsFlow.tryEmit(emptyList()) // TODO LOADING

				try {
					val result = getDataLoaderAndLoad(queryFilter)
					if (result.isEmpty())
						_canLoadMore = false
					else {
						values.plusAssign(result)
						itemsFlow.tryEmit(values)
					}
				} catch (e: Exception) {
					_canLoadMore = false
					// TODO How to feed this to UI
					logE("Cannot load more", e)
				}

				currentMaxPage++
			}

			private suspend fun getDataLoaderAndLoad(queryFilter: QueryFilter): List<ACatalogNovelUI> {
				return if (queryFilter.query.isEmpty()) {
					logV("Loading listing data")
					getCatalogueListingData(
						ext!!,
						HashMap<Int, Any>().apply {
							putAll(ext!!.searchFiltersModel.mapify())
							putAll(queryFilter.filters)
							this[PAGE_INDEX] = currentMaxPage
						}
					)
				} else {
					logV("Loading query data")
					loadCatalogueQueryDataUseCase(
						ext!!,
						queryFilter.query,
						HashMap<Int, Any>().apply {
							putAll(ext!!.searchFiltersModel.mapify())
							putAll(queryFilter.filters)
							this[PAGE_INDEX] = currentMaxPage
						}
					)
				}
			}
		}
	}

	private data class QueryFilter(
		var query: String,
		var filters: Map<Int, Any>
	)

	override fun setExtensionID(extensionID: Int) {
		when {
			extensionIDFlow.value == -1 ->
				logI("Setting NovelID")
			extensionIDFlow.value != extensionID ->
				logI("NovelID not equal, resetting")
			extensionIDFlow.value == extensionID -> {
				logI("Ignore if the same")
				return
			}
		}
		extensionIDFlow.tryEmit(extensionID)
	}

	override fun applyQuery(newQuery: String) {
		queryState = newQuery
		stateManager = StateManager()
		stateManager.loadMore()
	}

	@Synchronized
	override fun loadMore() {
		stateManager.loadMore()
	}

	override fun resetView() {
		itemsFlow.tryEmit(emptyList())
		queryState = ""
		filterDataState.clear()
		applyFilter()
	}

	override fun backgroundNovelAdd(novelID: Int): Flow<BackgroundNovelAddProgress> =
		flow {
			emit(BackgroundNovelAddProgress.ADDING)
			backgroundAddUseCase(novelID)
			emit(BackgroundNovelAddProgress.ADDED)
		}

	override fun applyFilter() {
		stateManager = StateManager()
		stateManager.loadMore()
	}

	override fun destroy() {
		queryState = ""
		extensionIDFlow.value = -1
		itemsFlow.tryEmit(emptyList())
		// TODO LOADING
		filterDataState.clear()
		stateManager = StateManager()
	}


	override fun getFilterStringState(id: Filter<String>): Flow<String> =
		filterDataState.specialGetOrPut(id.id) {
			MutableStateFlow(id.state)
		}

	override fun setFilterStringState(id: Filter<String>, value: String) {
		filterDataState.specialGetOrPut(id.id) {
			MutableStateFlow(id.state)
		}.tryEmit(value)
	}

	override fun getFilterBooleanState(id: Filter<Boolean>): Flow<Boolean> =
		filterDataState.specialGetOrPut(id.id) {
			MutableStateFlow(id.state)
		}

	override fun setFilterBooleanState(id: Filter<Boolean>, value: Boolean) {
		filterDataState.specialGetOrPut(id.id) {
			MutableStateFlow(id.state)
		}.tryEmit(value)
	}

	override fun getFilterIntState(id: Filter<Int>): Flow<Int> =
		filterDataState.specialGetOrPut(id.id) {
			MutableStateFlow(id.state)
		}

	override fun setFilterIntState(id: Filter<Int>, value: Int) {
		filterDataState.specialGetOrPut(id.id) {
			MutableStateFlow(id.state)
		}.tryEmit(value)
	}

	override fun resetFilter() {
		filterDataState.clear()
		launchIO {
			filterReloadFlow.emit(!filterReloadFlow.value)
			applyFilter()
		}
	}

	override fun setViewType(cardType: NovelCardType) {
		launchIO { setNovelUIType(cardType) }
	}


	private val novelCardTypeFlow by lazy { loadNovelUITypeUseCase() }

	override val novelCardTypeLive: Flow<NovelCardType> by lazy {
		novelCardTypeFlow.transformLatest {
			_novelCardType = it
			emit(it)
		}
	}

	private var _novelCardType: NovelCardType = NovelCardType.NORMAL

	override val novelCardType: NovelCardType
		get() = _novelCardType

	private var columnP: Int = SettingKey.ChapterColumnsInPortait.default

	private var columnH: Int = SettingKey.ChapterColumnsInLandscape.default

	init {
		launchIO {
			loadNovelUIColumnsHUseCase().collect {
				columnH = it
			}
		}

		launchIO {
			loadNovelUIColumnsPUseCase().collect {
				columnP = it
			}
		}
	}

	override val columnsInH
		get() = columnH

	override val columnsInP
		get() = columnP

	/**
	 * @param [V] Value type of the hash map
	 * @param [O] Expected value type
	 */
	private inline fun <reified O, V> HashMap<Int, V>.specialGetOrPut(
		key: Int,
		getDefaultValue: () -> O
	): O {
		// Do not use computeIfAbsent on JVM8 as it would change locking behavior
		return this[key].takeIf { value -> value is O }?.let { value -> value as O }
			?: getDefaultValue().also { defaultValue ->
				@Suppress("UNCHECKED_CAST")
				put(key, defaultValue as V)
			}
	}
}



