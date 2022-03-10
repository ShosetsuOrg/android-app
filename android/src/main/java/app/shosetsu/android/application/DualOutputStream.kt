package app.shosetsu.android.application

import java.io.IOException
import java.io.OutputStream

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
 * Shosetsu
 *
 * @since 19 / 10 / 2021
 * @author Doomsdayrs
 */
class MultipleOutputStream(private vararg val outputStreams: OutputStream) : OutputStream() {
	/**
	 * Iterates between each [OutputStream] in [outputStreams] and writes
	 */
	@Throws(IOException::class)
	override fun write(p0: Int) {
		outputStreams.forEach {
			it.write(p0)
		}
	}
}