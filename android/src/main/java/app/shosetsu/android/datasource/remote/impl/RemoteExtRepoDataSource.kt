package app.shosetsu.android.datasource.remote.impl

import app.shosetsu.android.common.ext.quickie
import app.shosetsu.android.datasource.remote.base.IRemoteExtRepoDataSource
import app.shosetsu.android.domain.model.local.RepositoryEntity
import app.shosetsu.lib.exceptions.HTTPException
import app.shosetsu.lib.json.RepoIndex
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.OkHttpClient
import java.net.SocketTimeoutException
import java.net.UnknownHostException

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
 * 13 / 05 / 2020
 */
class RemoteExtRepoDataSource(
	private val client: OkHttpClient,
) : IRemoteExtRepoDataSource {
	@OptIn(ExperimentalSerializationApi::class)
	@Throws(
		HTTPException::class,
		SocketTimeoutException::class,
		UnknownHostException::class,
	)
	override suspend fun downloadRepoData(
		repo: RepositoryEntity,
	): RepoIndex =
		@Suppress("BlockingMethodInNonBlockingContext")
		RepoIndex.fromString(
			client.quickie(
				"${repo.url}/index.json"
			).body!!.string()
		)
}