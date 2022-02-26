package app.shosetsu.android.common.ext

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
 * ====================================================================
 */

/**
 * shosetsu
 * 15 / 04 / 2020
 *
 * @author github.com/doomsdayrs
 */

fun Boolean.toInt(): Int = if (this) 1 else 0

@Deprecated("Why?", ReplaceWith("if (this) { action } else { null }"))
infix fun <T> Boolean.ifSo(action: T): T? = if (this) action else null
