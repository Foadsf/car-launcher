# Contributing

Thanks for helping. This launcher stays useful by staying small, so a few ground rules:

- **Keep it minimal.** It is a car home screen: big targets, few choices, glanceable. Features
  that add settings screens, widgets or theming will probably be declined. Open an issue to
  discuss a feature first.
- **No network access, ever.** No `INTERNET` permission, no analytics, no crash reporting, no ads.
  CI fails if `INTERNET` appears in the APK.
- **No proprietary dependencies.** Everything must be FLOSS and buildable from source, so the app
  can be packaged for F-Droid.
- **Pin versions exactly.** Bump a dependency in `gradle/libs.versions.toml` deliberately, one
  change per PR, and say why.
- **Touch targets stay ≥ 120 dp**, in portrait and landscape.

## Pull requests

1. Build locally with `./gradlew assembleDebug`. CI runs the same build on every PR.
2. Test on a real phone if you can. Say which device and Android version, and what you checked:
   which tiles appear, what each tap opened, and both orientations for layout changes.
3. Adding a tile? Add its package to both `TILES` in `Tiles.kt` and `<queries>` in
   `AndroidManifest.xml`.

By contributing, you agree that your work is licensed under the GPL-3.0.
