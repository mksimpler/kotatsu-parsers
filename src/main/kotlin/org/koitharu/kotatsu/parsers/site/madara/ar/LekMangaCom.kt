package org.koitharu.kotatsu.parsers.site.madara.ar

import org.jsoup.nodes.Document
import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.MangaChapter
import org.koitharu.kotatsu.parsers.model.MangaParserSource
import org.koitharu.kotatsu.parsers.site.madara.MadaraParser
import org.koitharu.kotatsu.parsers.util.*
import java.text.SimpleDateFormat

@MangaSourceParser("LEKMANGACOM", "LekMangaCom", "ar")
internal class LekMangaCom(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.LEKMANGACOM, "lekmanga.com") {
	override val listUrl = "lekmanga/"
	override val postReq = true

	override suspend fun loadChapters(mangaUrl: String, document: Document): List<MangaChapter> {
		val mangaId = document.select("div#manga-chapters-holder").attr("data-id")
		val url = "https://$domain/wp-admin/admin-ajax.php"
		val postData = "action=manga_get_chapters&manga=$mangaId"
		val doc = webClient.httpPost(url, postData).parseHtml()
		val dateFormat = SimpleDateFormat(datePattern, sourceLocale)
		return doc.select(selectChapter).mapChapters { i, li ->
			val a = li.selectFirst("a")
			val href = a?.attrAsRelativeUrlOrNull("href") ?: li.parseFailed("Link is missing")
			val link = href + stylePage
			val dateText = li.selectFirst("a.c-new-tag")?.attr("title") ?: li.selectFirst(selectDate)?.text()
			val name = a.selectFirst("p")?.text() ?: a.ownText()
			MangaChapter(
				id = generateUid(href),
				url = link,
				title = name,
				number = i + 1f,
				volume = 0,
				branch = null,
				uploadDate = parseChapterDate(
					dateFormat,
					dateText,
				),
				scanlator = null,
				source = source,
			)
		}
	}
}
