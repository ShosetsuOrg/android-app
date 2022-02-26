package app.shosetsu.android.datasource.local.database.impl

import android.database.sqlite.SQLiteException
import app.shosetsu.android.common.ext.toDB
import app.shosetsu.android.providers.database.dao.NovelsDao
import app.shosetsu.common.GenericSQLiteException
import app.shosetsu.common.datasource.database.base.IDBNovelsDataSource
import app.shosetsu.common.domain.model.local.LibraryNovelEntity
import app.shosetsu.common.domain.model.local.NovelEntity
import app.shosetsu.common.domain.model.local.StrippedNovelEntity
import app.shosetsu.common.dto.convertList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

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
 * 12 / 05 / 2020
 */
class DBNovelsDataSource(
	private val novelsDao: NovelsDao,
) : IDBNovelsDataSource {
	override suspend fun loadBookmarkedNovels(): List<NovelEntity> = try {
		(novelsDao.loadBookmarkedNovels().convertList())
	} catch (e: SQLiteException) {
		throw GenericSQLiteException(e)
	}

	override fun loadBookmarkedNovelsFlow(
	): Flow<List<LibraryNovelEntity>> = flow {
		try {
			emitAll(novelsDao.loadBookmarkedNovelsFlow())
		} catch (e: SQLiteException) {
			throw GenericSQLiteException(e)
		}
	}

	override suspend fun getNovel(novelID: Int): NovelEntity? = try {
		novelsDao.getNovel(novelID)?.convertTo()
	} catch (e: SQLiteException) {
		throw GenericSQLiteException(e)
	}

	override suspend fun getNovelFlow(novelID: Int): Flow<NovelEntity?> = flow {
		try {
			emitAll(novelsDao.getNovelFlow(novelID).map { it?.convertTo() })
		} catch (e: SQLiteException) {
			throw GenericSQLiteException(e)
		}
	}

	override suspend fun update(novelEntity: NovelEntity): Unit = try {
		(novelsDao.update(novelEntity.toDB()))
	} catch (e: SQLiteException) {
		throw GenericSQLiteException(e)
	}

	override suspend fun update(
		list: List<LibraryNovelEntity>
	): Unit = try {
		(novelsDao.update(list))
	} catch (e: SQLiteException) {
		throw GenericSQLiteException(e)
	}

	override suspend fun insertReturnStripped(
		novelEntity: NovelEntity,
	): StrippedNovelEntity? = try {
		novelsDao.insertReturnStripped(novelEntity.toDB())?.convertTo()
	} catch (e: SQLiteException) {
		throw GenericSQLiteException(e)
	}

	override suspend fun insert(novelEntity: NovelEntity): Long = try {
		(novelsDao.insertAbort(novelEntity.toDB()))
	} catch (e: SQLiteException) {
		throw GenericSQLiteException(e)
	}

	override suspend fun clearUnBookmarkedNovels(): Unit = try {
		(novelsDao.clearUnBookmarkedNovels())
	} catch (e: SQLiteException) {
		throw GenericSQLiteException(e)
	}

	override fun loadNovels(): List<NovelEntity> = try {
		(novelsDao.loadNovels().convertList())
	} catch (e: SQLiteException) {
		throw GenericSQLiteException(e)
	}
}