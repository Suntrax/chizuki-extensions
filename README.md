# Chizuki Extensions

Official template repository for creating background scraper extensions for the [**Chizuki**](https://github.com/Suntrax/chizuki) Android movie & TV show client.

## How it works

Chizuki extensions are headless Android apps (no UI) that resolve a playable stream URL for a given title.

1. **Discovery** — Chizuki scans installed apps for a `BroadcastReceiver` listening on `com.blissless.movieclient.EXTENSION_BEACON` whose app label starts with `"Chizuki: "`. Both are declared in this template's `AndroidManifest.xml`.
2. **Query** — When the user picks a movie or episode, Chizuki calls the extension's `ContentProvider` at:
   ```
   content://<packageName>.provider/scrape?title=<name>&tmdbId=<id>&mediaType=<movie|tv>[&season=N&episode=M]
   ```
   `tmdbId` and `mediaType` are always sent. `season` and `episode` are sent for TV shows only.
3. **Scrape** — The extension fetches the target website and extracts the stream URL(s) — typically HLS `.m3u8` playlists.
4. **Return** — The result is serialized to JSON and returned in a single-row `MatrixCursor`. Chizuki's `Media3 ExoPlayer` plays the first URL.

## Creating a new extension

1. Click **"Use this template"** at the top of this repository to create a new repo for your extension.
2. Clone your new repo and open it in Android Studio.
3. In Android Studio, press `Ctrl+Shift+R` (or `Cmd+Shift+R` on Mac) to open **Replace in Path**.
   - Search for `com.blissless.chizuki_extension_template` and replace it with your new package name (e.g., `com.blissless.movies67`).
   - Search for `TEMPLATE_NAME` and replace it with your extension's display name (e.g., `67movies`). This string is used in the app label (`Chizuki: TEMPLATE_NAME`) — Chizuki's discovery filter requires the label to start with `Chizuki: `.
4. Move your Kotlin files into the new package folder structure (e.g., `com/blissless/movies67/`). Also update the `-keep` rule in `app/src/main/keepRules/rules.keep` to match your new package.
5. Configure release signing. The `release` build type reads keystore credentials from `local.properties` (already git-ignored). Add four keys there:
   ```properties
   storeFile=release.jks
   storePassword=********
   keyAlias=********
   keyPassword=********
   ```
   `storeFile` is resolved relative to the project root, so the keystore can live anywhere — `release.jks`, `app/release.jks`, etc. Never commit the keystore itself; `*.jks` and `*.keystore` are already in `.gitignore`.
6. Open `TemplateScraper.kt` (rename it if you like) and implement your scraping logic!

## Data contract

Chizuki's `ExtensionManager` queries your `ContentProvider` via:

```
content://<packageName>.provider/scrape?title=<name>&tmdbId=<id>&mediaType=<movie|tv>[&season=N&episode=M]
```

**Query parameters** (read from the URI's query string):

| Param | Required | Description |
|-------|----------|-------------|
| `title` | Yes | The movie or TV show title (e.g., `"Interstellar"`, `"Breaking Bad"`). |
| `tmdbId` | Yes | The TMDB ID of the content (e.g., `"157336"`). Extensions can use this to skip their own TMDB search and construct embed URLs directly. |
| `mediaType` | Yes | Either `"movie"` or `"tv"`. Lets extensions construct the correct embed URL without guessing from the presence/absence of season/episode. |
| `season` | No | Season number — sent for TV shows only (e.g., `"1"`). |
| `episode` | No | Episode number — sent for TV shows only (e.g., `"1"`). |

The query returns a single-row `MatrixCursor` whose `"data"` column holds a JSON string.

### Success — array of stream URLs

The Chizuki player looks for an array of URLs under the `"Auto"` key (it also accepts `"auto"`, `"default"`, or `"urls"`). The first element is played by ExoPlayer:

```json
{
  "Auto": [
    "https://example.com/path/to/playlist.m3u8"
  ]
}
```

If you found multiple stream variants, return them all in the same `"Auto"` array — Chizuki will use the first one:

```json
{
  "Auto": [
    "https://example.com/.../1080p.m3u8",
    "https://example.com/.../720p.m3u8"
  ]
}
```

### Success — single-URL fallback

If your extension only has one URL, you may return it under any of the keys `"url"`, `"stream"`, `"m3u8"`, or `"playlist"`. Chizuki accepts these as a fallback when no `"Auto"` array is present:

```json
{ "url": "https://example.com/path/to/playlist.m3u8" }
```

### Error — any failure

Return a JSON object with an `"error"` key. Chizuki logs the message and shows the user that no stream could be resolved:

```json
{ "error": "No title provided." }
```
```json
{ "error": "No results found for '<title>'." }
```
```json
{ "error": "Failed to fetch stream: <network or parse error>" }
```

## Building

Extensions are built to be as tiny as possible (target: under 50 KB).
- Do not add any external dependencies (no OkHttp, no Jsoup, no Gson). Use Android's built-in `HttpURLConnection`, `WebView`, and `org.json`.
- R8 shrinking rules are stored in `app/src/main/keepRules/rules.keep`.
- Always build the **Release APK** (`./gradlew assembleRelease`) to ensure R8 shrinks the APK size.

Output: `app/build/outputs/apk/release/app-release.apk`

Install alongside the Chizuki main app:

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

Chizuki will pick it up automatically — install the extension, open Chizuki → Settings, and select your extension from the list.

## Architecture

| File | Purpose |
|------|---------|
| `TemplateScraper.kt` | All HTTP + HTML/JSON parsing logic. Returns a `Map<String, *>` with `"Auto"` (success) or `"error"` (failure). |
| `ScraperProvider.kt` | `ContentProvider` entry point. Reads `title`/`tmdbId`/`mediaType`/`season`/`episode` from the URI, passes them through, serializes the scraper result to JSON. |
| `ExtensionBeaconReceiver.kt` | Empty `BroadcastReceiver` for discovery by the Chizuki main app. |
| `AndroidManifest.xml` | Declares the `INTERNET` permission, the `ContentProvider` (exported), and the `EXTENSION_BEACON` receiver with the `"Chizuki: "` label prefix. |
| `keepRules/rules.keep` | R8 shrinking rules — keeps the `JavascriptInterface` bridge (for `WebView` scrapers) and all classes in your package. |

## Reference implementations

- [67movies-extension](https://github.com/Suntrax/67movies-extension) — movie/TV stream scraper
- [mangadotnet-extension](https://github.com/Suntrax/mangadotnet-extension) — manga chapter scraper (different return shape; for the Oni client, not Chizuki — included for code-style reference only)
