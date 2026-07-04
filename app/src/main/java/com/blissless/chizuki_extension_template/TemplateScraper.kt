package com.blissless.chizuki_extension_template

import android.content.Context

/**
 * Template scraper — implement your site's scraping logic here.
 *
 * Called by [ScraperProvider] with the query parameters received from the
 * Chizuki main app:
 *   - `title`     — Movie or TV show title (always sent).
 *   - `tmdbId`    — TMDB ID of the content (always sent). Use this to skip
 *                   your own TMDB search and construct embed URLs directly.
 *   - `mediaType` — `"movie"` or `"tv"` (always sent). Use this to construct
 *                   the correct embed URL without guessing.
 *   - `season`    — Season number, or `null` for movies.
 *   - `episode`   — Episode number, or `null` for movies.
 *
 * Return a `Map<String, *>` (preferred) or a `List<*>` (legacy flat list).
 * [ScraperProvider.serializeResult] handles the JSON conversion either way.
 *
 * Chizuki's player reads the first URL from the `"Auto"` array (it also
 * accepts `auto`/`default`/`urls` arrays, or the single-URL keys `url`/
 * `stream`/`m3u8`/`playlist` as a fallback). On failure, return a map with
 * an `"error"` key.
 *
 * Reference implementation: https://github.com/Suntrax/67movies-extension
 */
object TemplateScraper {

    fun scrape(
        context: Context,
        title: String?,
        tmdbId: Int?,
        mediaType: String?,
        season: Int?,
        episode: Int?
    ): Any {
        // TODO: Implement your scraping logic here.
        //
        // `tmdbId` and `mediaType` are provided by Chizuki — you can use them
        // to construct embed URLs directly without searching the target site.
        // For example, if your target site uses TMDB IDs in its URLs:
        //
        // val embedUrl = if (mediaType == "tv") {
        //     "https://example.com/embed/tv/$tmdbId/${season ?: 1}/${episode ?: 1}"
        // } else {
        //     "https://example.com/embed/movie/$tmdbId"
        // }
        //
        // Then load `embedUrl` in a WebView and intercept the m3u8.

        // Example returning a single HLS stream URL:
        // return mapOf(
        //     "Auto" to listOf("https://example.com/path/to/playlist.m3u8")
        // )

        // Example returning an error:
        // return mapOf("error" to "No title provided.")

        return mapOf("error" to "TemplateScraper.scrape() not implemented.")
    }
}
