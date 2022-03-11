package app.shosetsu.android.view.uimodels.model.reader

import android.view.View
import androidx.core.view.isVisible
import app.shosetsu.android.view.uimodels.base.BindViewHolder
import com.github.doomsdayrs.apps.shosetsu.R
import com.github.doomsdayrs.apps.shosetsu.databinding.ChapterDividerBinding

/**
 * Divides each chapter, signalling what is from before and what is next
 * Will always appear after the first chapter, never before the first.
 * Will appear after the last chapter, but stating there are no more chapters
 */
data class ReaderDividerUI(
	val prev: String,
	val next: String? = null
) : ReaderUIItem<ReaderDividerUI, ReaderDividerUI.ViewHolder>() {
	override var identifier: Long
		get() = (prev.hashCode() + next.hashCode()).toLong()
		set(_) {}

	override val layoutRes: Int = R.layout.chapter_divider
	override val type: Int = R.layout.chapter_divider

	override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		if (!super.equals(other)) return false

		other as ReaderDividerUI

		if (prev != other.prev) return false
		if (next != other.next) return false

		return true
	}

	override fun hashCode(): Int {
		var result = super.hashCode()
		result = 31 * result + prev.hashCode()
		result = 31 * result + (next?.hashCode() ?: 0)
		return result
	}

	class ViewHolder(view: View) : BindViewHolder<ReaderDividerUI, ChapterDividerBinding>(view) {
		override val binding: ChapterDividerBinding =
			ChapterDividerBinding.bind(view)

		override fun ChapterDividerBinding.bindView(item: ReaderDividerUI, payloads: List<Any>) {
			if (item.next == null) {
				noMoreChapters.isVisible = true
				previousChapterCont.isVisible = false
				nextChapterCont.isVisible = false
				return
			}
			previousChapter.text =
				binding.root.context.getString(R.string.reader_last_chapter, item.prev)
			nextChapter.text =
				binding.root.context.getString(R.string.reader_next_chapter, item.next)
		}

		override fun ChapterDividerBinding.unbindView(item: ReaderDividerUI) {
			previousChapterCont.isVisible = true
			nextChapterCont.isVisible = true
			noMoreChapters.isVisible = false
			nextChapter.text = null
			previousChapter.text = null
		}
	}


}
