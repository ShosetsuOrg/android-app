package app.shosetsu.common.domain.repositories.impl

import app.shosetsu.common.datasource.database.base.IDBChaptersDataSource
import app.shosetsu.common.datasource.file.base.IFileCachedChapterDataSource
import app.shosetsu.common.datasource.file.base.IFileChapterDataSource
import app.shosetsu.common.datasource.memory.base.IMemChaptersDataSource
import app.shosetsu.common.datasource.remote.base.IRemoteChaptersDataSource
import app.shosetsu.common.domain.model.local.ChapterEntity
import app.shosetsu.common.domain.model.local.ReaderChapterEntity
import app.shosetsu.common.domain.repositories.base.IChaptersRepository
import app.shosetsu.lib.IExtension
import app.shosetsu.lib.Novel
import kotlinx.coroutines.flow.Flow

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
 * 02 / 05 / 2020
 * @param memorySource Source from memory
 * @param dbSource Source from db
 * @param remoteSource Source from online
 * @param fileSource Source from storage
 */
class ChaptersRepository(
	private val memorySource: IMemChaptersDataSource,
	private val cacheSource: IFileCachedChapterDataSource,
	private val dbSource: IDBChaptersDataSource,
	private val fileSource: IFileChapterDataSource,
	private val remoteSource: IRemoteChaptersDataSource,
) : IChaptersRepository {

	private suspend inline fun placeIntoCache(
		entity: ChapterEntity,
		chapterType: Novel.ChapterType,
		result: ByteArray
	) = saveChapterPassageToMemory(entity, chapterType, result)

	override suspend fun getChapterPassage(
		formatter: IExtension,
		entity: ChapterEntity,
	): ByteArray {
		return try {
			memorySource.loadChapterFromCache(entity.id!!)!!
		} catch (e: Exception) {
			try {
				cacheSource.loadChapterPassage(entity.id!!, formatter.chapterType).also { result ->
					memorySource.saveChapterInCache(entity.id!!, result)
				}
			} catch (e: Exception) {
				try {
					fileSource.load(entity, formatter.chapterType)
						.also { placeIntoCache(entity, formatter.chapterType, it) }
				} catch (e: Exception) {
					remoteSource.loadChapterPassage(
						formatter,
						entity.url
					).also { placeIntoCache(entity, formatter.chapterType, it) }
				}
			}
		}
	}

	suspend fun saveChapterPassageToMemory(
		chapterEntity: ChapterEntity,
		chapterType: Novel.ChapterType,
		passage: ByteArray,
	) {
		memorySource.saveChapterInCache(chapterEntity.id!!, passage)
		cacheSource.saveChapterInCache(chapterEntity.id!!, chapterType, passage)
	}


	/**
	 *
	 * 1. save to memory
	 * 2. save to filesystem
	 * 3. if filesystem save was a success, then update the chapter
	 * 4. finally evaluate with and between 1&&(2||3)
	 */

	/**
	 * We want to ensure that the [passage] is saved either to [memorySource] or [fileSource] first off
	 * After that, then we can update the [entity] to say [passage] was properly saved
	 */
	override suspend fun saveChapterPassageToStorage(
		entity: ChapterEntity,
		chapterType: Novel.ChapterType,
		passage: ByteArray,
	): Unit {
		saveChapterPassageToMemory(entity, chapterType, passage)

		fileSource.save(entity, chapterType, passage)

		dbSource.updateChapter(entity.copy(isSaved = true))
	}

	override suspend fun handleChapters(
		novelID: Int,
		extensionID: Int, list: List<Novel.Chapter>,
	): Unit =
		dbSource.handleChapters(novelID, extensionID, list)

	override suspend fun handleChaptersReturn(
		novelID: Int,
		extensionID: Int, list: List<Novel.Chapter>,
	): List<ChapterEntity> =
		dbSource.handleChapterReturn(novelID, extensionID, list)

	override suspend fun getChaptersLive(novelID: Int): Flow<List<ChapterEntity>> =
		dbSource.getChaptersFlow(novelID)

	override suspend fun getChapters(novelID: Int): List<ChapterEntity> =
		dbSource.getChapters(novelID)

	override suspend fun getChaptersByExtension(extensionId: Int): List<ChapterEntity> =
		dbSource.getChaptersByExtension(extensionId)

	override suspend fun updateChapter(chapterEntity: ChapterEntity): Unit =
		dbSource.updateChapter(chapterEntity)

	override suspend fun getChapter(chapterID: Int): ChapterEntity? =
		dbSource.getChapter(chapterID)

	override suspend fun getReaderChaptersFlow(
		novelID: Int,
	): Flow<List<ReaderChapterEntity>> = dbSource.getReaderChapters(novelID)

	override suspend fun updateReaderChapter(readerChapterEntity: ReaderChapterEntity): Unit =
		dbSource.updateReaderChapter(readerChapterEntity)

	override suspend fun deleteChapterPassage(
		chapterEntity: ChapterEntity,
		chapterType: Novel.ChapterType
	) {
		dbSource.updateChapter(
			chapterEntity.copy(
				isSaved = false
			)
		)
		fileSource.delete(chapterEntity, chapterType)
	}


	override suspend fun delete(entity: ChapterEntity) =
		dbSource.delete(entity)

}