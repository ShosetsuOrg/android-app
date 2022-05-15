package app.shosetsu.android.datasource.remote.base

import app.shosetsu.common.domain.model.local.GenericExtensionEntity
import app.shosetsu.common.domain.model.local.RepositoryEntity
import app.shosetsu.lib.exceptions.HTTPException

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
interface IRemoteExtensionDataSource {

	/**
	 * Downloads an extension
	 * @return extension content
	 */
	@Throws(HTTPException::class)
	suspend fun downloadExtension(
		repositoryEntity: RepositoryEntity,
		extensionEntity: GenericExtensionEntity,
	): ByteArray
}