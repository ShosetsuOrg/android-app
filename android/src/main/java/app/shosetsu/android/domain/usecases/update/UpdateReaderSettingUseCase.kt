package app.shosetsu.android.domain.usecases.update

import app.shosetsu.common.GenericSQLiteException
import app.shosetsu.common.domain.model.local.NovelReaderSettingEntity
import app.shosetsu.common.domain.repositories.base.INovelReaderSettingsRepository

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
 * 26 / 02 / 2021
 */
class UpdateReaderSettingUseCase(
	private val repo: INovelReaderSettingsRepository
) {

	@Throws(GenericSQLiteException::class)
	suspend operator fun invoke(entity: NovelReaderSettingEntity) =
		repo.update(entity)
}