# Changelog

## [1.3.0]
- Builds without a local Android Studio install: CI compiles against IntelliJ IDEA Community; a local IDE is optional (`-PlocalIdePath`).
- Generation runs as a cancellable background task with progress and a result notification.
- The action is shown only for `res` folders.
- Fixed a crash when pressing OK without choosing images.
- Fixed `strings.xml` corruption for app names containing `'`, `&`, `<` or `$`.
- Adaptive icon layers are rendered at the correct 108 dp size (sharper icons on all launchers).
- Smooth (anti-aliased) edges for round and rounded icons.
- Falls back to PNG when the IDE has no WebP encoder and removes stale duplicates of the other format.
- Unit tests and GitHub Actions CI.

## [1.2.0]
- Faster icon drawing.

## [1.1.0]
- Localized app name generation.
- Icon and app-name generation separated, files written concurrently.

## [1.0.0]
- Initial release: localized launcher icons for all densities and adaptive icon XML.
