package app.shosetsu.android.domain.usecases.get

import app.shosetsu.android.common.ext.convertTo
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.domain.usecases.ConvertNCToCNUIUseCase
import app.shosetsu.android.view.uimodels.model.catlog.ACatalogNovelUI
import app.shosetsu.common.GenericSQLiteException
import app.shosetsu.common.LuaException
import app.shosetsu.common.consts.settings.SettingKey
import app.shosetsu.common.domain.repositories.base.IExtensionSettingsRepository
import app.shosetsu.common.domain.repositories.base.INovelsRepository
import app.shosetsu.common.domain.repositories.base.ISettingsRepository
import app.shosetsu.lib.IExtension
import javax.net.ssl.SSLException

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
 * 15 / 05 / 2020
 */
class GetCatalogueListingDataUseCase(
	private val novelsRepository: INovelsRepository,
	private val convertNCToCNUIUseCase: ConvertNCToCNUIUseCase,
	private val settingsRepo: ISettingsRepository,
	private val extSettingsRepo: IExtensionSettingsRepository
) {
	@Throws(SSLException::class, LuaException::class)
	suspend operator fun invoke(
		iExtension: IExtension,
		data: Map<Int, Any>
	): List<ACatalogNovelUI> =
		settingsRepo.getInt(SettingKey.SelectedNovelCardType).let { cardType ->
			extSettingsRepo.getSelectedListing(iExtension.formatterID)
				.let { selectedListing ->
					// Load catalogue data

					novelsRepository.getCatalogueData(
						iExtension,
						selectedListing,
						data
					).let { list ->
						list.map { novelListing ->
							novelListing.convertTo(iExtension)
						}.mapNotNull { ne ->
							// For each, insert and return a stripped card
							// This operation is to pre-cache URL and ID so loading occurs smoothly
							try {
								novelsRepository.insertReturnStripped(ne)?.let { result ->
									convertNCToCNUIUseCase(result, cardType)
								}
							} catch (e: GenericSQLiteException) {
								logE("Failed to load parse novel", e)
								null
							}
						}
					}
				}
		}
}