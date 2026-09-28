# PipChat — AI Forex & Gold Technical Analyst

[![Build & Release Debug APK](https://github.com/FajarAriandi17/pipchat-v.0.1/actions/workflows/build-apk.yml/badge.svg)](https://github.com/FajarAriandi17/pipchat-v.0.1/actions/workflows/build-apk.yml)
[![GitHub Release](https://img.shields.io/github/v/release/FajarAriandi17/pipchat-v.0.1?color=E9B44C&label=Latest%20Release)](https://github.com/FajarAriandi17/pipchat-v.0.1/releases)
[![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B-3DDC84.svg?logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-4285F4.svg?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)

Asisten Analisa Teknikal Forex & Emas berbasis kecerdasan buatan (Gemini AI + Tool Calling) dengan visualisasi grafik candlestick interaktif dan antarmuka Jetpack Compose modern berstandar Material 3.

---

## 📲 Download Langsung APK Debug (Direct Download)

Anda dapat mengunduh langsung file APK debug tanpa perlu meng-compile secara manual:

| Versi | Tautan Unduhan Langsung | Sumber |
|---|---|---|
| **PipChat Debug APK (Terbaru)** | [⬇️ **Download `pipchat-debug.apk`**](https://github.com/FajarAriandi17/pipchat-v.0.1/releases/download/v0.1-preview/pipchat-debug.apk) | [GitHub Releases](https://github.com/FajarAriandi17/pipchat-v.0.1/releases) |
| **Build Artifacts** | Unduh dari tab **Actions** > Pilih workflow run terakhir > **Artifacts** | [GitHub Actions](https://github.com/FajarAriandi17/pipchat-v.0.1/actions) |

> 💡 **Cara Install di Ponsel Android**:
> 1. Download file `pipchat-debug.apk` ke HP Android Anda.
> 2. Buka file APK dan izinkan instalasi dari sumber tidak dikenal (*Allow from this source*) jika diminta.
> 3. Buka aplikasi **PipChat** dan pilih *"Lanjut sebagai Akun Demo (Tamu)"* atau login via Google / Apple / OTP.

---

## 🚀 Otomasi CI/CD GitHub Actions

Repositori ini telah dilengkapi alur kerja otomasi **GitHub Actions** (`.github/workflows/build-apk.yml`) yang melakukan:
- 🧪 **Eksekusi Unit Test**: Memverifikasi logika engine teknikal, perhitungan Fibonacci, dan manajemen model pengguna.
- 🔨 **Build APK Otomatis**: Menjalankan `./gradlew assembleDebug` pada runner Ubuntu dengan JDK 17 & Gradle Caching.
- 📦 **Penyimpanan Artifact**: Mengunggah `pipchat-debug.apk` ke tab Artifacts selama 30 hari.
- 🏷️ **GitHub Releases**: Otomatis merilis dan menyematkan APK ke halaman Release publik setiap kali ada push ke branch `main`, tag baru (`v*`), atau dipicu manual (*workflow dispatch*).

### Menjalankan Build Manual via GitHub Web:
1. Buka tab **Actions** di repositori GitHub Anda: `https://github.com/FajarAriandi17/pipchat-v.0.1/actions`
2. Pilih workflow **Build & Release Debug APK** di sebelah kiri.
3. Klik tombol **Run workflow**, pilih branch `main`, lalu klik **Run workflow**.
4. Setelah 1–2 menit, APK siap diunduh di tab Release atau Artifacts!

---

## ✨ Fitur Utama Aplikasi

### 1. 🤖 AI Forex & Gold Technical Analyst
- Analisa multi-timeframe (M15, H1, H4, D1) untuk pasangan mata uang utama (EUR/USD, GBP/USD, USD/JPY) dan Emas (XAU/USD).
- *Tool calling* otonom untuk kalkulasi level Support & Resistance (Pivot Points, Fibonacci retracement) dan pembentukan Trade Plan terstruktur.

### 2. 📊 Grafik Candlestick OHLC Interaktif
- Custom Canvas Jetpack Compose berperforma tinggi.
- Menampilkan garis level harga horizontal:
  - 🟢 **Entry Level**
  - 🔵 **Take Profit 1 (RR 1:1,5)** & **Take Profit 2 (RR 1:2)**
  - 🔴 **Stop Loss (Risk Managed)**
- Panel statistik indikator: RSI (14), EMA (50/200), MACD, dan status volatilitas ATR.

### 3. 🔐 Sistem Autentikasi Lengkap
- **Sign in with Apple**: Sesuai Apple Human Interface Guidelines dengan tombol warna hitam kontras dan dukungan masker email *Apple Private Relay* (`@privaterelay.appleid.com`).
- **Google OAuth**: Integrasi satu ketukan dengan avatar pengguna.
- **OTP Numerik 6-Digit**: Verifikasi instan tanpa kata sandi (*passwordless*) dengan timer hitung mundur kirim ulang.
- **Email & Kata Sandi**: Mode tradisional dengan validasi keamanan.
- **Akun Demo (Tamu)**: Langsung mencoba seluruh fitur tanpa login.

### 4. 💾 Penyimpanan Offline Lokal & Pengaturan
- **Room Database**: Menyimpan seluruh riwayat sesi percakapan secara lokal di perangkat.
- **Manajemen Risiko**: Pilihan batas risiko modal 1%, 2%, atau 3% per transaksi.
- **Dukungan Bahasa**: Bahasa Indonesia & English.

---

## 📄 Lisensi
Hak Cipta © 2026 M FAJAR ARIANDI. Dibuat dengan Google AI Studio.
