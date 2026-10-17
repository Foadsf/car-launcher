# Car Launcher

A minimal, free Android home screen for an old phone that lives in a car: navigation, music, phone,
nothing else.

- A big clock and date
- A now-playing strip with previous / play-pause / next, for whichever app is playing
- One large tile per app: CoMaps, Google Maps, TomTom, HERE WeGo, Music (Twelve), Phone, Settings
- A tile only shows when its app is installed and enabled, and the grid updates live as apps come
  and go
- Dark and high-contrast, with every touch target at least 120 dp. Works in portrait and landscape.

**No network permission, no analytics, no ads, no tracking.** CI fails any build that adds
`INTERNET`. Licensed under the [GPL-3.0](LICENSE).

I wrote it because F-Droid had no FLOSS car launcher. It was built for, and tested on, a Galaxy S9
running /e/OS 4.3 (Android 15). It needs Android 10 (API 29) or newer.

## Install

Grab `app-debug.apk` from the latest successful [build run](../../actions/workflows/build.yml)
(under *Artifacts*), or build it yourself (below). Then:

```bash
adb install -r app-debug.apk
```

On the phone:

1. **Make it the home screen:** *Settings → Apps → Default apps → Home app → Car Launcher.* It
   also appears in the app drawer, so you can try it first.
2. **Enable the music controls:** tap the amber *"Tap to show music controls"* strip. It opens Car
   Launcher's own *Allow notification access* page. Android only lets an app see other apps' media
   sessions with this access. The listener service is empty and reads no notifications (see
   [`MediaListener.kt`](app/src/main/java/io/github/foadsf/carlauncher/MediaListener.kt)). If the
   toggle is greyed out ("restricted setting"), use *App info → ⋮ → Allow restricted settings*
   first.

With nothing playing, the play button sends a media *play* key, which resumes whichever player
last had the audio.

## Build

Needs Java 17+ and an Android SDK with **platform `android-37.0`** and **build-tools `37.0.0`**.
AndroidX core 1.19 and Compose 1.12 require compiling against API 37. That is only the *compile*
SDK: `targetSdk` is 35 and `minSdk` is 29.

```bash
sdkmanager "platforms;android-37.0" "build-tools;37.0.0"
```
```bash
ANDROID_HOME=$HOME/Android/Sdk ./gradlew assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/`. The Gradle wrapper fetches Gradle 9.8.1 and checks
its SHA-256. Every plugin and library version is pinned in
[`gradle/libs.versions.toml`](gradle/libs.versions.toml).

### Signing

Debug builds are signed with your machine's own `~/.android/debug.keystore`. If you build on
several machines and want each build to update the installed copy, put a shared `debug.keystore`
(alias `androiddebugkey`, passwords `android`) next to `settings.gradle.kts` and it is used
instead. It is gitignored: **never commit a signing key here.** Anyone holding the key that signed
your installed copy can ship an "update" to an app that has notification access.

## Changing the tiles

Edit `TILES` in
[`Tiles.kt`](app/src/main/java/io/github/foadsf/carlauncher/Tiles.kt). An app tile takes a list
of package names and launches the first one installed. TomTom already lists both
`com.tomtom.gplay.navapp.gofleet` and `com.tomtom.gplay.navapp`. **Add every new package to
`<queries>` in [`AndroidManifest.xml`](app/src/main/AndroidManifest.xml) too.** Without that,
Android 11+ hides the package from the app and its tile never appears.

## Contributing

Issues and pull requests are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md).
