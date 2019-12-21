package com.github.doomsdayrs.apps.shosetsu.backend.async

import android.os.AsyncTask
import android.util.Log
import com.github.Doomsdayrs.api.shosetsu.services.core.dep.Formatter
import com.github.Doomsdayrs.api.shosetsu.services.core.objects.Novel
import com.github.doomsdayrs.apps.shosetsu.backend.Utilities.wait
import com.github.doomsdayrs.apps.shosetsu.backend.scraper.WebViewScrapper


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
 * 20 / 12 / 2019
 *
 * @author github.com/doomsdayrs
 */
open class CatalogueLoader(val formatter: Formatter) : AsyncTask<Int, Void, List<Novel>>() {

    var query: String = ""

    constructor(query: String, formatter: Formatter) : this(formatter) {
        this.query = query
    }

    /**
     * Loads up the category
     *
     * @param integers if length = 0, loads first page otherwise loads the page # correlated to the integer
     * @return if this was completed or not
     */
    override fun doInBackground(vararg integers: Int?): List<Novel> {
        Log.d("Loading", "Catalogue")
        if (formatter.hasCloudFlare()) {
            Log.i("CatalogueLoader", "CLOUDFLARE DETECED")
            wait(5)
        }
        // Loads novel list
        return if (integers.isEmpty())
            formatter.parseLatest(WebViewScrapper.docFromURL(formatter.getLatestURL(1), formatter.hasCloudFlare()))
        else {
            formatter.parseLatest(WebViewScrapper.docFromURL(formatter.getLatestURL(integers[0]!!), formatter.hasCloudFlare()))
        }
    }
}