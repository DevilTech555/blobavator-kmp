# Blobavatar for Kotlin Multiplatform (KMP) & Compose Multiplatform

Deterministic geometric blobatars/blobavatars from any string. This is the official Kotlin Multiplatform (KMP) port of [blobatar](https://blobatar.dev), adhering to the frozen generation-2 seed-to-look contract with 100% test parity against the upstream test vectors.

The same string and options always produce the exact same avatar across Android, Desktop (JVM), iOS, macOS, Web (WasmJs / JS), and Linux.

---

## Architecture & Modules

The library is organized into distinct layers:

### 1. `blobavatar-core`
- **Package**: `com.navbyte.blobavatar.core`
- **Pure Kotlin Multiplatform**: Zero third-party runtime dependencies.
- **Targets**: Android, JVM, iOS (`iosArm64`, `iosSimulatorArm64`, `iosX64`), macOS (`macosArm64`, `macosX64`), Web (`wasmJs`, `js`), Linux (`linuxX64`).
- **Features**:
  - Deterministic Unicode NFC normalization and 32-bit Murmur3 avalanche + independent trait streaming.
  - OKLCh color palette calculations with real sRGB WCAG contrast enforcement (4.5:1 text floor for eyes, surface floor for dark backgrounds).
  - All 10 generation-2 shapes: `round`, `organic`, `boxy`, `capsule`, `nub`, `cloud`, `droplet`, `hexagon`, `sun`, `triangle`.
  - Precise geometry calculations (Superellipse, Catmull-Rom spline, arc, polygon, box, droplet taper).
  - 14 expressions: `idle`, `happy`, `sad`, `mad`, `surprised`, `wink`, `sleepy`, `smug`, `unsure`, `scared`, `love`, `shy`, `sick`, `thinking`.
  - Deterministic elapsed-time motion mathematics (breathe, bob, blink, saccade, thinking rock, mad tremor, hover reaction).
  - Headless SVG generator (`Blobavatar.toSvg(...)`) for server-side, CLI, or web applications.

### 2. `blobavatar-compose`
- **Package**: `com.navbyte.blobavatar.compose`
- **Compose Multiplatform UI**: Native rendering directly via Compose Canvas primitives (`DrawScope` / `Path`).
- **Targets**: Android, Desktop (JVM), iOS (`iosArm64`, `iosSimulatorArm64`), macOS (`macosArm64`), Web (`wasmJs`, `js`).
- **Features**:
  - `Blobavatar(...)`: Static Composable.
  - `AnimatedBlobavatar(...)`: Full animated Composable with ambient breathing, blinking, saccades, gaze, pointer hover lift, and smooth easing transitions between expressions.
  - Compatibility aliases: `Blobatar`, `AnimatedBlobatar`, `BlobatarOptions`.

### 3. `sample`
- **Package**: `com.navbyte.blobavatar.sample`
- **Interactive Multiplatform Studio**: Preview studio supporting Desktop and Android with real-time controls, auto-cycle emotion animations, and live SVG inspector.

---

## Installation

### Gradle (Kotlin DSL)

```kotlin
// In your build.gradle.kts (commonMain)
dependencies {
    // For pure multiplatform calculations & SVG generation:
    implementation("com.navbyte:blobavatar-core:<version>")

    // For Compose Multiplatform UI:
    implementation("com.navbyte:blobavatar-compose:<version>")
}
```

---

## Usage Guide

### Compose Multiplatform

#### Static Avatar

```kotlin
import androidx.compose.ui.unit.dp
import com.navbyte.blobavatar.compose.Blobavatar
import com.navbyte.blobavatar.core.Backdrop
import com.navbyte.blobavatar.core.BlobavatarOptions
import com.navbyte.blobavatar.core.happy

@Composable
fun UserProfile(userEmail: String) {
    Blobavatar(
        name = userEmail,
        size = 64.dp,
        options = BlobavatarOptions(
            background = Backdrop.CIRCLE, // Circle backdrop plate
            expression = happy
        ),
        contentDescription = "User avatar"
    )
}
```

#### Animated Avatar

```kotlin
import androidx.compose.ui.unit.dp
import com.navbyte.blobavatar.compose.AnimatedBlobavatar
import com.navbyte.blobavatar.compose.BlobatarAnimation
import com.navbyte.blobavatar.core.Backdrop
import com.navbyte.blobavatar.core.BlobavatarOptions
import com.navbyte.blobavatar.core.thinking

@Composable
fun InteractiveProfile(userEmail: String) {
    // Ambient breathing, blinking, and saccades with hover lift:
    AnimatedBlobavatar(
        name = userEmail,
        size = 96.dp,
        animation = BlobatarAnimation.Hover,
        options = BlobavatarOptions(
            background = Backdrop.CIRCLE,
            expression = thinking
        )
    )

    // Continuous ambient motion:
    AnimatedBlobavatar(
        name = userEmail,
        size = 96.dp,
        animation = BlobatarAnimation.Always,
        options = BlobavatarOptions(
            background = Backdrop.CIRCLE
        )
    )
}
```

---

## Headless & SVG Generation (`blobavatar-core`)

You can generate SVG strings anywhere (Android, desktop JVM, Ktor server, iOS, Wasm, JS) with zero UI dependencies:

```kotlin
import com.navbyte.blobavatar.core.Backdrop
import com.navbyte.blobavatar.core.Blobavatar
import com.navbyte.blobavatar.core.BlobavatarOptions
import com.navbyte.blobavatar.core.love

// Render deterministic SVG string:
val svg = Blobavatar.toSvg(
    name = "alain@example.com",
    options = BlobavatarOptions(
        background = Backdrop.CIRCLE,
        expression = love
    ),
    size = 120
)
```

---

## Customizing Options

All options are immutable data classes:

```kotlin
val options = BlobavatarOptions(
    // Backdrop plate: Backdrop.NONE, Backdrop.CIRCLE (default in studio), Backdrop.SQUIRCLE, Backdrop.SQUARE
    background = Backdrop.CIRCLE,

    // Lock hue in degrees (0..360), so the seed drives shape only
    hue = 210.0,

    // Lock tone in swatch set (0.0..1.0, pastel to ink)
    tone = 0.8,

    // Palette overrides
    palette = mapOf("head" to "#ff5722", "eye" to "#ffffff"),

    // Pin specific trait values [0, 1)
    traits = mapOf(
        "shape" to 0.95, // Lock to sun shape
        "eye.ratio" to 0.0 // Round eyes
    ),

    // WCAG contrast enforcement
    contrast = true,

    // Unicode NFC + whitespace trim + lowercase
    normalize = true,

    // Expression pose (idle, happy, sad, mad, surprised, wink, sleepy, smug, unsure, scared, love, shy, sick, thinking)
    expression = happy,

    // Emotion-specific procedural personality motion (joyful hop, heartbeat pulse, rage tremor, drowsy nods, etc.)
    animateEmotions = true
)
```

---

## Interactive Sample App (`:sample`)

The project includes an interactive studio app built with Compose Multiplatform supporting Desktop (macOS, Windows, Linux) and Android:

- **Features**:
  - **Circle Backdrop Plate Default**: Avatars rendered on circular plates by default with instant toggle between `CIRCLE`, `SQUIRCLE`, `SQUARE`, and `NONE`.
  - **Auto-Cycle Emotions Animation Toggle**: Automatically iterates through all 14 emotional poses (`idle`, `happy`, `sad`, `mad`, `surprised`, `wink`, `sleepy`, `smug`, `unsure`, `scared`, `love`, `shy`, `sick`, `thinking`) with smooth spring/tween morphing of eyes, mouths, and colors in real-time.
  - **Configurable Cycle Speed**: Choose between Fast (1.2s), Normal (1.8s), and Relaxed (2.5s).
  - **Live Avatar Preview**: Displays ambient breathing, bobbing, blinking, saccades, and hover lift.
  - **Interactive Seed Input & Presets**: Test arbitrary emails, usernames, or quick presets.
  - **Live OKLCh Tuning**: Real-time hue (0°–360°) and tone (pastel to ink) sliders.
  - **Collapsible SVG Markup Viewer**: Instant preview and copy of generated SVG markup.
  - **Avatar Crowd Gallery**: Visual demonstration of deterministic variety across different seeds.

### Running the Sample App

```sh
# Run on Desktop (macOS / JVM):
./gradlew :sample:run

# Build Android Debug APK:
./gradlew :sample:assembleDebug
# Output APK: sample/build/outputs/apk/debug/sample-debug.apk
```

---

## Parity & Verification

The core engine is rigorously tested against `reference-vectors.json` (exported from TypeScript blobatar `v2.4.0`):
- **1,570+ test cases** covering every silhouette band (`round`, `organic`, `boxy`, `capsule`, `nub`, `cloud`, `droplet`, `hexagon`, `sun`, `triangle`).
- **Exact byte parity** for normalization, Murmur3 seeds, streaming trait floats, OKLCh hex outputs, and SVG path data.
- **Floating-point tolerance** bounded within $10^{-9}$ for IEEE 754 cross-engine trigonometry calculations.

Run tests:
```sh
./gradlew :blobavatar-core:jvmTest
./gradlew :blobavatar-compose:jvmTest
```

---

## Building

```sh
# Build JVM JARs
./gradlew :blobavatar-core:jvmJar :blobavatar-compose:jvmJar

# Build Android AARs
./gradlew :blobavatar-core:assembleRelease :blobavatar-compose:assembleRelease

# Publish to local Maven cache (~/.m2/repository)
./gradlew publishToMavenLocal
```
