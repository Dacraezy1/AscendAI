# ⚡ AscendAI — Looksmaxxing & Facial Aesthetics Engine

[![Build APK](https://github.com/Dacraezy1/AscendAI/actions/workflows/android.yml/badge.svg)](https://github.com/Dacraezy1/AscendAI/actions/workflows/android.yml)
[![Platform](https://img.shields.io/badge/Platform-Android_8.0%2B-00F0FF.svg)](https://developer.android.com)
[![Engine](https://img.shields.io/badge/Biometrics-Google_ML_Kit-8B5CF6.svg)](https://developers.google.com/ml-kit)
[![Cost](https://img.shields.io/badge/Cost-100%25_Free_%26_Open_Source-10B981.svg)](#)

> **AscendAI** is an ultra-modern, 100% free Android application inspired by Umax and LooksMax AI, designed to analyze your facial architecture, determine your exact aesthetic tier, and guide your ascension with science-backed protocols.

---

## 📱 How to Download & Install the APK

No local build setup or Android Studio required on your machine! GitHub Actions automatically compiles and packages the APK on every update.

1. Go to the **[Actions Tab](https://github.com/Dacraezy1/AscendAI/actions)** of this repository.
2. Click on the latest workflow run (e.g., **`Build AscendAI Android APK`**).
3. Scroll down to the **Artifacts** section at the bottom of the page.
4. Click on **`AscendAI-Debug-APK`** to download the ZIP file.
5. Extract the ZIP to get `app-debug.apk` and transfer it to your phone (or download directly from your phone's browser).
6. Tap the APK file to install and ascend!

---

## 🌟 Key Features

### 1. 🧬 Neural Biometric Face Analysis
- **Dual Perspective Scanning**: Capture or upload both **Front Profile** (facial thirds, symmetry, eye area) and **Side Profile** (gonial angle, chin projection, jawline flare).
- **Google ML Kit Integration**: Utilizes 468 on-device landmark detections to calculate facial ratios without sending any photos to external servers.
- **Canthal Tilt Calculation**: Precise angular measurement of your periorbital vector (degrees of positive or negative tilt for hunter vs. tired eyes).
- **Bilateral Symmetry Index**: Quantifies hemifacial balance across your vertical nasal midline.
- **Facial Thirds & Golden Ratio**: Analyzes upper brow third, midface nasal third, and lower mandible third against the 1.618 golden ratio.

### 2. 👑 Looksmax Tier Classification
AscendAI ranks your current facial harmony on a 0–100 scale, and assigns your aesthetic tier:

| Tier | Score Range | Description |
| :--- | :---: | :--- |
| **Sub-5** | `0 – 49` | Foundational phase. Tremendous room for growth via debloating, posture, and skin barrier healing. |
| **LTN** (Low Tier Normie) | `50 – 59` | High potential base. Holding water retention or unoptimized grooming. |
| **MTN** (Mid Tier Normie) | `60 – 69` | Balanced harmony. A few targeted aesthetic tweaks away from standout tier. |
| **HTN** (High Tier Normie) | `70 – 79` | Prominent cheekbones, sharp jawline, and favorable eye spacing. Stands out in daily life. |
| **Chadlite** | `80 – 89` | Elite bone structure, hollow cheeks, positive canthal tilt. Top 5% territory. |
| **Chad** | `90 – 95` | Top 1% dimorphic harmony. Razor-sharp mandible, predatory hunter eyes, optimal thirds. |
| **True Adam** | `96 – 100` | Apex aesthetic perfection. Absolute symmetry, chiseled maxilla, and god-tier facial presence. |

### 3. 🎯 Potential Score & Radar Harmony Matrix
- **Potential Rating (/100)**: Calculates your maximum reachable aesthetic ceiling after leanness, masseter conditioning, and skincare optimization.
- **6-Axis Biometric Radar Chart**: Interactive visual polygon illustrating your balance across:
  - *Jawline & Mandible*
  - *Eye Area & Canthal Tilt*
  - *Facial Bilateral Symmetry*
  - *Facial Thirds & Harmony*
  - *Skin Clarity & Tone*
  - *Cheekbone Prominence*

### 4. 📚 Scientific Ascension Guides
Step-by-step biological guides with actionable instructions:
- **Mewing & Palatal Posture**: Correct tongue suction on the palatine bone to project the maxilla and expand the dental arch.
- **Debloating & Sodium Balance**: Lowering aldosterone with 3.5L water flushes, 4:1 potassium-to-sodium ratio, and lymphatic ice rolling.
- **Hunter Eyes Protocol**: Eyelid muscle hypertrophy (orbicularis oculi), sleeping on back, and caffeine vasoconstriction.
- **Masseter Hypertrophy**: Hard gum resistance chewing and deep cervical neck curls to widen the gonial angle.
- **Glass Skin & Collagen Synthesis**: Retinoid cycling (Tretinoin 0.05%), broad-spectrum SPF 50+, and lipid barrier restoration.
- **Haircut Face Synergy**: Tailoring fades and crops to balance oblong, square, and diamond face shapes.

### 5. 🔥 Daily Habit & Streak Tracker
- Interactive checklist to track your daily looksmaxxing discipline:
  - 👅 *Tongue on Palate (Mewing 24/7)*
  - 💧 *Hydration Flush (3.5 Liters)*
  - ☀️ *AM Skincare & SPF 50*
  - 🧊 *Face Cold Plunge / Ice Roll*
  - 🏋️ *Chin Tucks & Posture Stretches*
  - 🌙 *PM Retinoid & Barrier Cream*
  - 🛌 *Back Sleeping (8 Hours)*
- Streak counter with fire badges to maintain ascension momentum.

### 6. 🕒 Scan History & Progress
- Keep an ongoing timeline of your scans.
- Compare previous ratings, potential gains, and metric changes as you ascend over time.

---

## 🔒 Privacy & Permissions
- **Camera (`android.permission.CAMERA`)**: Used strictly to capture live front and side facial photos.
- **Photo Media / Storage (`READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE`)**: Used only when you choose to import an existing photo from your gallery.
- **100% On-Device Processing**: No accounts, no paywalls, and no photos are ever uploaded to cloud servers.

---

## 🛠️ Architecture & Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3) with Dark Cyberpunk aesthetics
- **Vision Engine**: Google ML Kit Face Detection (Accurate landmark & contour model)
- **Image Pipeline**: Coil Compose + AndroidX CameraX + FileProvider
- **Architecture**: MVVM (Model-View-ViewModel) with StateFlow & Coroutines
- **CI/CD**: GitHub Actions (`ubuntu-latest`, JDK 17 Temurin, Gradle Wrapper 8.7)

---

## 📄 License
This project is open-source and free under the [MIT License](LICENSE).
