package app.shosetsu.android.viewmodel.impl.settings

import androidx.work.WorkInfo
import app.shosetsu.android.backend.workers.onetime.DownloadWorker
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.view.uimodels.settings.base.SettingsItemData
import app.shosetsu.android.view.uimodels.settings.dsl.*
import app.shosetsu.android.viewmodel.abstracted.settings.ADownloadSettingsViewModel
import app.shosetsu.common.consts.settings.SettingKey.*
import app.shosetsu.common.domain.repositories.base.ISettingsRepository
import com.github.doomsdayrs.apps.shosetsu.R
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine

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
 * 31 / 08 / 2020
 */
class DownloadSettingsViewModel(
	iSettingsRepository: ISettingsRepository,
	private val manager: DownloadWorker.Manager
) : ADownloadSettingsViewModel(iSettingsRepository) {

	override suspend fun settings(): List<SettingsItemData> = listOf(
		seekBarSettingData(6) {
			titleText = "Download thread pool size"
			descText = "How many simultaneous downloads occur at once"
			range { 1F to 6F }

			progressValue = settingsRepo.getInt(DownloadThreadPool).toFloat()

			showSectionMark = true
			showSectionText = true

			seekBySection = true
			seekByStepSection = true
			autoAdjustSectionMark = true
			touchToSeek = true
			hideBubble = true

			sectionC = 5
			array.apply {
				put(0, "1")
				put(1, "2")
				put(2, "3")
				put(3, "4")
				put(4, "5")
				put(5, "6")

			}
			onProgressChanged { _, progress, _, fromUser ->
				if (fromUser) launchIO {
					settingsRepo.setInt(DownloadThreadPool, progress)
				}
			}
		},

		seekBarSettingData(6) {
			titleText = "Download threads per Extension"
			descText = "How many simultaneous downloads per extension that can occur at once"
			range { 1F to 6F }

			progressValue = settingsRepo.getInt(DownloadExtThreads).toFloat()

			showSectionMark = true
			showSectionText = true

			seekBySection = true
			seekByStepSection = true
			autoAdjustSectionMark = true
			touchToSeek = true
			hideBubble = true

			sectionC = 5
			array.apply {
				put(0, "1")
				put(1, "2")
				put(2, "3")
				put(3, "4")
				put(4, "5")
				put(5, "6")

			}
			onProgressChanged { _, progress, _, fromUser ->
				if (fromUser) launchIO {
					settingsRepo.setInt(DownloadExtThreads, progress)
				}
			}
		},
		// TODO Figure out how to change download directory
		switchSettingData(3) {
			title { R.string.download_chapter_updates }
			checkSettingValue(DownloadNewNovelChapters)
		},
		switchSettingData(2) {
			title { "Allow downloading on metered connection" }
			checkSettingValue(DownloadOnMeteredConnection)
		},
		switchSettingData(3) {
			title { "Download on low battery" }
			checkSettingValue(DownloadOnLowBattery)
		},
		switchSettingData(4) {
			title { "Download on low storage" }
			checkSettingValue(DownloadOnLowStorage)
		},
		switchSettingData(5) {
			title { "Download only when idle" }
			requiredVersion { android.os.Build.VERSION_CODES.M }
			checkSettingValue(DownloadOnlyWhenIdle)
		},
		switchSettingData(6) {
			title { "Bookmarked novel on download" }
			description { "If a novel is not bookmarked when a chapter is downloaded, this will" }
		},
		switchSettingData(7) {
			titleRes = R.string.settings_download_notify_extension_install_title
			descRes = R.string.settings_download_notify_extension_install_desc
			checkSettingValue(NotifyExtensionDownload)
		},
		seekBarSettingData(9) {
			titleRes = R.string.settings_download_delete_on_read_title
			descRes = R.string.settings_download_delete_on_read_desc
			range { 0F to 5F }

			progressValue = settingsRepo.getInt(DeleteReadChapter).toFloat() + 1

			showSectionMark = true
			showSectionText = true

			seekBySection = true
			seekByStepSection = true
			autoAdjustSectionMark = true
			touchToSeek = true
			hideBubble = true

			sectionC = 4
			array.apply {
				put(0, "Disabled")
				put(1, "Delete current")
				put(2, "Previous")
				put(3, "2nd to last")
				put(4, "3rd to last")

			}
			onProgressChanged { _, progress, _, fromUser ->
				if (fromUser) launchIO {
					settingsRepo.setInt(DeleteReadChapter, progress - 1)
				}
			}
		},
		switchSettingData(10) {
			titleText = "Unique chapter notification"
			descText = "Create a notification for each chapters status when downloading"
			checkSettingValue(DownloadNotifyChapters)
		},

		)

	override var downloadWorkerSettingsChanged: Boolean = false

	init {
		launchIO {
			var ran = false
			settingsRepo.getBooleanFlow(DownloadOnlyWhenIdle)
				.combine(settingsRepo.getBooleanFlow(DownloadOnLowStorage)) { _, _ -> }
				.combine(settingsRepo.getBooleanFlow(DownloadOnLowBattery)) { _, _ -> }
				.combine(settingsRepo.getBooleanFlow(DownloadOnMeteredConnection)) { _, _ -> }
				.collect {
					if (!ran) {
						ran = true
						return@collect
					}

					if (manager.count != 0 && manager.getWorkerState() == WorkInfo.State.ENQUEUED)
						downloadWorkerSettingsChanged = true
				}
		}
	}

	override fun restartDownloadWorker() {
		manager.stop()
		manager.start()
	}
}