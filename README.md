<p align="center">
  <img src="app/src/main/res/mipmap-xxhdpi/ic_launcher.png" width="120" alt="Screen Translate Icon"/>
</p>

<h1 align="center">Screen Translate</h1>

<p align="center">
  <strong>Point. Tap. Understand.</strong><br>
  <em>Instantly translate any text on your Android screen — right where it appears.</em>
</p>

<p align="center">
  <a href="https://github.com/Yellow4Submarine7/screen-trans/releases">
    <img src="https://img.shields.io/github/v/release/Yellow4Submarine7/screen-trans?style=for-the-badge&logo=github" alt="GitHub Release"/>
  </a>&nbsp;
  <img src="https://img.shields.io/badge/min%20SDK-23-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Min SDK 23"/>&nbsp;
  <img src="https://img.shields.io/badge/Kotlin-2.x-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin"/>
</p>

---

## About this fork

This is a personal republished copy of [`AidanPark/android-screen-translator`](https://github.com/AidanPark/android-screen-translator), independently maintained as `Yellow4Submarine7/screen-trans`. It is **not** distributed via Google Play — release builds are side-loadable debug-signed APKs published under [Releases](https://github.com/Yellow4Submarine7/screen-trans/releases). A future major version is planned as a complete rewrite in a different language/stack; until then this Kotlin/Compose codebase is kept buildable and patched for new-device quirks (foldables, current Android behavior).

---

## The Problem

You're reading a webpage in Japanese. Or watching a Korean drama without subtitles. Or trying to use a Chinese app. Every time, you have to:

1. Long-press the text (if you even can)
2. Copy it
3. Switch to a translator app
4. Paste it
5. Read the translation
6. Switch back

**What if you could just... point at it?**

---

## How It Works

Screen Translate floats on top of whatever you're doing. Just drag the pointer to any text, and the translation appears instantly — like magic.

<table>
  <tr>
    <td align="center" width="50%">
      <strong>Word Mode</strong><br>
      <em>Tap any word for instant translation</em><br><br>
      <img src=".images/word_original_rounded.gif" width="280" alt="Word Mode"/>
    </td>
    <td align="center" width="50%">
      <strong>Sentence Mode</strong><br>
      <em>Auto-detects and translates full sentences</em><br><br>
      <img src=".images/sentence_original_rounded.gif" width="280" alt="Sentence Mode"/>
    </td>
  </tr>
  <tr>
    <td align="center">
      <strong>Paragraph Mode</strong><br>
      <em>Translates entire blocks of text at once</em><br><br>
      <img src=".images/paragraph_original_rounded.gif" width="280" alt="Paragraph Mode"/>
    </td>
    <td align="center">
      <strong>Selection Mode</strong><br>
      <em>Draw a box around exactly what you need</em><br><br>
      <img src=".images/select_original_rounded.gif" width="280" alt="Selection Mode"/>
    </td>
  </tr>
</table>

A fifth **Fixed-Area** mode pins translation to a sub-region of the screen and keeps re-translating it as content changes — useful for games or video with subtitles in a stable location.

---

## Works Everywhere

<p align="center">
  <img src=".images/screenshots/02.png" width="220" alt="Works with every app"/>
  &nbsp;&nbsp;
  <img src=".images/screenshots/05.png" width="220" alt="Select what you need"/>
  &nbsp;&nbsp;
  <img src=".images/screenshots/06.png" width="220" alt="Even comics"/>
</p>

<p align="center">
  News apps, social media, games, manga, webtoons — if there's text on screen, it can be translated.
</p>

---

## Features

**Capture & OCR**
- Live screen capture via `MediaProjection` with optimized frame streaming
- On-device OCR for **Latin, Chinese, Japanese, Korean, and Devanagari** scripts via Google ML Kit
- Five detection modes: Word, Sentence, Paragraph, Selection box, Fixed-Area
- Custom text grouping that clusters detected lines into words, sentences, or paragraphs from position, spacing, and font size
- Right-to-left and vertical-writing language support (Arabic, Hebrew, Japanese vertical, etc.)
- Automatic source-language detection via ML Kit Language ID

**Translation engines**
- Google Translate (web/JS endpoint plus on-device ML Kit translate models)
- DeepL
- Microsoft Azure Translator
- Papago (Naver)
- Engine choice is per-language, with each engine's supported pairs determined at runtime

**AI assist**
- Optional ChatGPT-based OCR error correction (`data/remote/ai/CorrectionRepository.kt`) — sends the recognized text to ChatGPT to clean up misread characters before translation
- Bring-your-own OpenAI API key

**UX**
- Text-to-Speech with selectable voices and rate
- Floating overlay built in Jetpack Compose, rendered through `SYSTEM_ALERT_WINDOW`
- Reactive screen-metrics handling for foldables and orientation changes
- Onboarding flow + permission requesters for notifications and screen-capture grants

---

## Translation Engines

| Engine | Provider | Notes |
|---|---|---|
| Google Translate | Google | Default, broad language coverage; also uses on-device ML Kit translate models |
| DeepL | DeepL SE | Higher-quality output for European language pairs (requires your own API key) |
| Azure Translator | Microsoft | Wide language list including rare codes (Klingon, Yucatec Maya, etc.) |
| Papago | Naver | Strong on Korean / Japanese / Chinese pairs |

A stub `YandexKit` exists in source but is not wired into the production engine list.

---

## AI-Powered Accuracy

<p align="center">
  <img src=".images/screenshots/01.png" width="250" alt="AI text correction"/>
</p>

OCR isn't perfect — characters get misread, especially with small fonts, handwriting, or stylized text. When enabled, Screen Translate sends the recognized text to **ChatGPT** to repair OCR mistakes before handing it off to a translation engine, which materially improves results on messy input.

> **Note:** In this fork's debug build, AI Correction requires a user-supplied OpenAI API key — enter it via **Settings → API Key** in the app. Without a key, ML Kit's on-device Google Translate still works; everything else (including the upcoming Sense Group mode) needs a key. The original backend-distributed key mechanism is intentionally inert here and will return when the project is publicly distributed.

---

## Under the Hood

> *For curious developers who want to peek inside.*

### How the magic happens

1. **Screen Capture** — `MediaProjection` continuously captures the current display into an `ImageReader` surface
2. **Text Recognition** — Google ML Kit runs **on-device OCR** on captured frames (5 script-specific recognizers)
3. **Smart Grouping** — A custom algorithm clusters detected text into words, lines, sentences, or paragraphs based on geometry, spacing, font size, and writing direction
4. **Translation** — Recognized text is sent to the chosen engine (Google web/ML Kit, DeepL, Azure, or Papago) via Retrofit
5. **Overlay Rendering** — Results are rendered in a floating Compose overlay attached as a `TYPE_APPLICATION_OVERLAY` window

### Tech highlights

| | |
|---|---|
| **Kotlin + Jetpack Compose** | Modern Android UI with reactive state management |
| **MVVM + Repository pattern** | ViewModels and repositories for separation of concerns |
| **Hilt** | Dependency injection across the app |
| **ML Kit** | On-device OCR for Latin, Chinese, Japanese, Korean, and Devanagari + Language ID + on-device translate models |
| **Multi-engine translation** | Google, DeepL, Azure, Papago via Retrofit + kotlinx-serialization |
| **Coroutines + StateFlow** | Async pipelines from capture → OCR → translate → overlay |
| **Firebase** | Analytics, Crashlytics, Remote Config, Realtime Database, App Check |
| **Play Integrity API** | Standard-request device integrity verification (`data/local/secure/SecureRepository.kt`) |
| **MediaProjection** | Real-time screen capture with `ImageReader`-backed frame pipeline |
| **androidx.window** | Foldable-aware screen-metrics tracking via `currentWindowMetrics` |

### Project structure

```
app/src/main/java/com/galaxy/airviewdictionary/
├── App.kt
├── Const.kt
├── core/                  # OverlayService — foreground service hosting the overlay
├── data/
│   ├── AVDRepository.kt
│   ├── local/
│   │   ├── capture/       # MediaProjection-based screen capture
│   │   ├── preference/    # DataStore-backed user settings
│   │   ├── screen/        # ScreenInfoHolder, foldable/orientation metrics
│   │   ├── secure/        # Play Integrity verdicts
│   │   ├── tts/           # Text-to-Speech
│   │   └── vision/        # ML Kit OCR + custom text grouping
│   └── remote/
│       ├── ai/            # ChatGPT OCR correction
│       │   └── chatgpt/
│       ├── billing/       # Google Play Billing
│       ├── firebase/      # Analytics, Crashlytics, Remote Config, RTDB
│       ├── geolocale/     # Region detection
│       └── translation/
│           ├── azure/
│           ├── deepl/
│           ├── goolge/    # (sic) Google web + ML Kit translate
│           ├── papago/
│           └── yandex/    # stub, not active
├── di/                    # Hilt modules
├── extensions/            # Kotlin extension helpers
└── ui/
    ├── common/
    ├── screen/
    │   ├── intro/         # Splash
    │   ├── main/          # Settings
    │   ├── onboarding/
    │   ├── overlay/       # Floating translation UI (the heart of the UX)
    │   ├── permissions/
    │   ├── reply/
    │   └── test/          # Internal dev screens
    └── theme/
```

---

## Recent fixes

### v2.4.5 (May 2026)

Two unrelated bug fixes bundled together.

- **Stale ViewModel cache in `OverlayService`.** After the service's `viewModelStore` was cleared, the manual `lateinit var` cache still pointed at the dead ViewModel — translations would silently stop working. Cache now invalidates with the store.
- **Foldable screen dimensions.** `ScreenInfoHolder.updateScreenInfoInService` previously only swapped width/height by orientation instead of re-reading actual display bounds, so after folding the bubble cursor landed off-screen on Z Fold-style devices. It now reads `WindowManager.currentWindowMetrics.bounds` on each configuration change.
- **Known behavior:** the post-fold MediaProjection re-authorization dialog is *intentional*, per Android's documented design. `VirtualDisplay.resize() + setSurface()` only covers same-display configuration changes; physical-display switches invalidate the existing projection.

### v2.4.3

- **Keep MediaProjection alive across screen lock.** An inactivity `Runnable` in `CaptureRepository` was nulling the projection token whenever the screen turned off, forcing users to re-grant screen recording on every wake. Removed that path; the projection now survives lock/unlock cycles for as long as the foreground service is running.

---

## Building from Source

### What you need

- **Android Studio** Ladybug or later
- **JDK 17**
- **Android SDK 35** (compile/target), min SDK 23

### Steps

1. **Clone the repo**
   ```bash
   git clone https://github.com/Yellow4Submarine7/screen-trans.git
   cd screen-trans
   ```

2. **Set up signing** — edit `gradle.properties`:
   ```properties
   KEYSTORE_FILE=your-keystore.jks
   KEY_ALIAS=your-key-alias
   KEY_PASSWORD=your-key-password
   ```

3. **Add Firebase config** — drop your own `google-services.json` in `app/`.

4. **Build & install**
   ```bash
   ./gradlew assembleDebug
   ```

> Debug builds use Google's official test AdMob App ID, so no AdMob account is required to run the app locally. Live translation engines other than ML Kit's on-device Google models require your own API keys (DeepL, Azure, Papago, OpenAI). OCR and on-device translation work without any keys.

---

## Permissions Explained

| Permission | What it does |
|---|---|
| `SYSTEM_ALERT_WINDOW` | Shows the floating translation overlay on top of other apps |
| `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_MEDIA_PROJECTION` | Hosts the long-running capture/overlay service |
| `MediaProjection` (runtime grant) | Captures the screen frame-by-frame for OCR |
| `INTERNET` + `ACCESS_NETWORK_STATE` | Calls remote translation and AI APIs |
| `POST_NOTIFICATIONS` | Required for the foreground-service notification on Android 13+ |
| `VIBRATE` | Haptic feedback when text is detected under the cursor |

---

## License

Provided for **educational and reference purposes**. See [LICENSE](LICENSE) for details.
