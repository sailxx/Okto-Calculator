<div align="center">

<img src="assets/readme/hero-en.svg" width="100%" alt="Okto Calc — an Android calculator in the Okto style with Google Calculator features">

<a href="https://github.com/sailxx/Okto-Calculator/releases/latest/download/OktoCalc.apk"><img src="assets/readme/btn-android-en.svg" height="52" alt="Download for Android"></a>&nbsp;&nbsp;<a href="https://github.com/sailxx/Okto-Calculator/releases/latest"><img src="assets/readme/btn-new-en.svg" height="52" alt="What’s new"></a>

**Okto Calc is an Android calculator with the feature set of Google Calculator.** Percent, brackets, a scientific mode and calculation history; dark and light themes in the style of [Okto](https://github.com/sailxx/Okto). It works offline, requests no permissions and contains no ads.

</div>

<br>

<img src="assets/readme/features-en.svg" width="100%" alt="Features: calculations, scientific mode, history, convenience">

<details>
<summary><b>Feature details</b></summary>

### Calculations

Percent is taken from the left operand, as in Google Calculator: `50 + 10%` = 55, `50 × 10%` = 5. The `( )` key picks an opening or closing bracket by itself, missing brackets are closed on evaluation, and implicit multiplication (`2π`, `3(1+3)`) is handled correctly. Powers, square root and factorial are supported. Results are rounded to 12 significant digits (`0.1 + 0.2` = 0.3); very large and very small numbers use `1.5E12` notation. Errors are reported in plain text: “Can't divide by 0”, “Invalid input”.

### Scientific mode

The **f(x)** button on the display opens a panel: `sin`, `cos`, `tan`, `ln`, `log`, `π`, `e`, `xʸ`, `x²`, `x⁻¹`, `x!`, `√`, `|x|`. **Inv** switches the functions to their inverses: `sin⁻¹`, `cos⁻¹`, `tan⁻¹`, `eˣ`, `10ˣ`, `x³`. Angles are in degrees or radians (the DEG/RAD switch); values such as `sin(180°)` yield an exact zero. In landscape orientation the scientific keys always sit to the left of the main ones.

### History

The last 100 calculations are stored on the device. Tapping a row inserts its result into the current expression; the list can be cleared.

### Convenience

The result is shown under the expression while you type. A long press on the display copies the result; a long press on the delete key clears the input. Digits of long numbers are grouped (`1 234 567`). Haptic feedback can be turned off in the settings.

</details>

<br>

<img src="assets/readme/themes-en.svg" width="100%" alt="Themes: Okto dark and light">

<details>
<summary><b>Appearance</b></summary>

Settings open with the “Settings” button on the display. Both themes use the [Okto](https://sailxx.github.io/Okto/) layout: round keys, a frameless display with large digits, Golos Text and JetBrains Mono fonts. The accent is the orange from the app icon: the equals key and the operator signs. A pressed round key squeezes into a rounded square.

- **Dark** — graphite background, light digits.
- **Light** — the same layout on a light background; the operator orange is darkened for legibility.

</details>

<br>

<img src="assets/readme/numbers-en.svg" width="100%" alt="By the numbers: 1.5 MB, 0 permissions, 0 ads, 2 themes, 15 scientific keys">

<details>
<summary><b>Installation</b></summary>

1. Download [`OktoCalc.apk`](https://github.com/sailxx/Okto-Calculator/releases/latest/download/OktoCalc.apk).
2. Open the file on your phone and allow installation from unknown sources.
3. Android 8.0 or newer is required.

</details>

<details>
<summary><b>Development</b></summary>

Kotlin 2.2 and Jetpack Compose (Material 3), no third-party libraries. The evaluator is a hand-written recursive-descent parser covered by unit tests.

```bash
./gradlew testDebugUnitTest   # evaluator tests
./gradlew assembleRelease     # APK in app/build/outputs/apk/release/
```

Requires JDK 17+ and the Android SDK (platform 35).

README images are generated in both languages (texts live in `tools/readme/strings.json`); the SVGs are not edited by hand:

```bash
cd tools/readme && npm install && node build.mjs
```

```
app/src/main/java/com/sailxx/oktocalc/
├── MainActivity.kt   — entry point, screens, theme
├── Engine.kt         — expression parsing, evaluation, formatting
├── CalcState.kt      — input, live preview, history
├── CalcScreen.kt     — display and layouts (portrait and landscape)
├── Keys.kt           — the key
├── Panels.kt         — history and settings
└── Theme.kt          — palettes, fonts, strings, settings
```

The Golos Text and JetBrains Mono fonts are distributed under the SIL Open Font License 1.1.

</details>
