package app.shosetsu.common.domain.repositories.base

import app.shosetsu.common.GenericSQLiteException
import app.shosetsu.common.domain.model.local.NovelReaderSettingEntity
import kotlinx.coroutines.flow.Flow

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
 * 24 / 02 / 2021
 */
interface INovelReaderSettingsRepository {
	@Throws(GenericSQLiteException::class)
	suspend fun get(novelID: Int): NovelReaderSettingEntity?

	fun getFlow(novelID: Int): Flow<NovelReaderSettingEntity?>

	@Throws(GenericSQLiteException::class)
	suspend fun insert(novelReaderSettingEntity: NovelReaderSettingEntity): Long

	@Throws(GenericSQLiteException::class)
	suspend fun update(novelReaderSettingEntity: NovelReaderSettingEntity)
}