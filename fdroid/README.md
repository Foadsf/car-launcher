# F-Droid submission draft

[`io.github.foadsf.carlauncher.yml`](io.github.foadsf.carlauncher.yml) is the metadata file for
[fdroiddata](https://gitlab.com/fdroid/fdroiddata). It lives here only as a draft: the copy F-Droid
uses is the one merged into fdroiddata. It passes `fdroid lint` and `fdroid rewritemeta` leaves it
unchanged.

The build pins the full commit hash of tag `v0.1.0`. The `prebuild` installs platform
`android-37.0` and build-tools `37.0.0`, because AndroidX core 1.19 and Compose 1.12 need
compileSdk 37. Gradle 9.8.1 is in F-Droid's
[gradle-transparency-log](https://gitlab.com/fdroid/gradle-transparency-log), so `gradlew-fdroid`
accepts it.

Store text and screenshots come from [`fastlane/`](../fastlane/metadata/android/en-US) in this
repo, not from fdroiddata.

## Submitting

1. Fork https://gitlab.com/fdroid/fdroiddata and clone the fork.
2. Copy this file to `metadata/io.github.foadsf.carlauncher.yml` on a new branch.
3. In the fdroiddata checkout, run `fdroid lint io.github.foadsf.carlauncher` and
   `fdroid rewritemeta io.github.foadsf.carlauncher`. Both should leave the file as it is.
4. Push the branch and open a merge request using the *App inclusion* template.

## Releasing a new version

Bump `versionCode` and `versionName` in `app/build.gradle.kts`, add
`fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`, commit, and push a `vX.Y.Z` tag.
With `UpdateCheckMode: Tags` and `AutoUpdateMode: Version`, F-Droid picks the tag up by itself.
