package app.shosetsu.android.datasource.local.database.base

import app.shosetsu.android.common.GenericSQLiteException
import app.shosetsu.common.domain.model.local.NovelSettingEntity
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
 * 01 / 01 / 2021
 *
 * Database source for [NovelSettingEntity]
 */
interface IDBNovelSettingsDataSource {
	fun getFlow(novelID: Int): Flow<NovelSettingEntity?>

	@Throws(GenericSQLiteException::class)
	suspend fun update(novelSettingEntity: NovelSettingEntity)

	@Throws(GenericSQLiteException::class)
	suspend fun get(novelID: Int): NovelSettingEntity?

	@Throws(GenericSQLiteException::class)
	suspend fun insert(novelSettingEntity: NovelSettingEntity): Long
}