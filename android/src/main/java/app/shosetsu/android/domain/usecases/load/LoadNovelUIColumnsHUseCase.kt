package app.shosetsu.android.domain.usecases.load

import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import kotlinx.coroutines.flow.transformLatest

/*
 * This file is part of Shosetsu.
 *
 * Shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Shosetsu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Shosetsu.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * shosetsu
 * 08 / 12 / 2020
 */
class LoadNovelUIColumnsHUseCase(
	private val iSettingsRepository: ISettingsRepository
) {
	operator fun invoke() = iSettingsRepository.getIntFlow(SettingKey.ChapterColumnsInLandscape)
		.transformLatest {
			if (it != 0)
				emit(it)
			else emit(6)
		}
}