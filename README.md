# Localized Launcher Icon

[![CI](https://github.com/Pobiwan007/LocalizedLauncherIcon/actions/workflows/ci.yml/badge.svg)](https://github.com/Pobiwan007/LocalizedLauncherIcon/actions/workflows/ci.yml)
[![Release](https://img.shields.io/github/v/release/Pobiwan007/LocalizedLauncherIcon)](https://github.com/Pobiwan007/LocalizedLauncherIcon/releases)
![Platform](https://img.shields.io/badge/Android%20Studio%20%2F%20IntelliJ-2024.2%2B-3DDC84?logo=androidstudio&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin&logoColor=white)

An Android Studio plugin that generates **locale-specific launcher icons and app names** in one step.
Useful when an app needs a different icon or name per market (e.g. a localized logo for `ru`, `kk-rKZ`
and `uz-rUZ`), which Android Studio's Image Asset wizard cannot do.

## 🖼️ Demo

![Demo of plugin usage](docs/demo.gif)

## 🚀 Features

- Full launcher icon set for every density (`mdpi` … `xxxhdpi`) and every selected locale:
  - `ic_launcher` (48 dp, rounded square) and `ic_launcher_round` (48 dp, circle)
  - `ic_launcher_foreground` / `ic_launcher_background` adaptive layers (108 dp)
  - `mipmap-<locale>-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml`
- Localized `app_name` in `values-<locale>/strings.xml`: creates the file, replaces an existing entry or
  appends one, with correct escaping of `'`, `&`, `<`, `@` and so on
- Many locales at once: `es-rCL, es-rAR, es-rPE, qu-rPE`
- Runs in the background with progress and a result notification, then refreshes the project tree
- Anti-aliased round/rounded edges; WebP output when the IDE supports it, PNG otherwise

## 🛠️ Installation

### From GitHub Releases

1. Download `LocalizedLauncherIcon-x.y.z.zip` from [Releases](https://github.com/Pobiwan007/LocalizedLauncherIcon/releases)
   (or the `LocalizedLauncherIcon-plugin` artifact of the latest CI run).
2. **Settings → Plugins → ⚙️ → Install Plugin from Disk…** and select the ZIP.
3. Restart the IDE.

### JetBrains Marketplace

> Coming soon…

## 🧪 How to use

1. In the Project view, right-click a module's **`res`** folder → **New → Create Localization**.
2. Enter one or more locales: `ru`, `kk-rKZ` or `es-rCL, es-rAR, qu-rPE`.
3. Tick **Add localized launcher icons** and pick the foreground and background images (PNG / WebP / JPEG),
   and/or tick **Add localized app name** and type the name.
4. Press **OK**. Files are generated in the background and the `res` folder is refreshed.

## 🏗️ How it works

```
LocalizedLauncherIconAction   New → Create Localization (visible only on res/)
        │
LLIADialogWrapper             Kotlin UI DSL form + validation
        │  Task.Backgroundable (progress, notification, VFS refresh)
        ├── LauncherGenerator   Java2D: scale, compose, alpha-mask → mipmap-<locale>-<density>/
        └── AppNameGenerator    values-<locale>/strings.xml (escape + upsert app_name)
validator/Locales.kt          pure parsing & validation, unit-tested
```

Icons for all locales and densities are rendered concurrently with coroutines on `Dispatchers.IO`.

## 👩‍💻 Development

```bash
./gradlew test buildPlugin          # tests + installable ZIP in build/distributions
./gradlew runIde                    # sandbox IntelliJ IDEA Community with the plugin
./gradlew runIde -PlocalIdePath="C:\Program Files\Android\Android Studio"   # your local Android Studio
```

Tip: put `localIdePath=...` into `~/.gradle/gradle.properties` to make it the default on your machine.

See [CHANGELOG.md](CHANGELOG.md) for release notes.
