# SubLingo - Subtitle Translator

A professional Android subtitle translation app using Google ML Kit for offline, on-device translation.

## Features
- Translate SRT/VTT subtitle files to any language
- Real-time video subtitle translation  
- Beautiful spiral progress animation
- 50+ language support (offline, no internet needed after model download)
- Translation history

## Build Instructions

### Setup
1. Clone the repository:
```bash
git clone https://github.com/elephantcos-cloud/sublingo-app
cd sublingo-app
```

2. Download Gradle wrapper (run once):
```bash
gradle wrapper --gradle-version 8.6
```

3. Build debug APK:
```bash
./gradlew assembleDebug
```

APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

### Push from Termux
```bash
cd /path/to/sublingo-app
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/elephantcos-cloud/sublingo-app.git
git push -u origin main
```

## Tech Stack
- Kotlin + Android Native
- Google ML Kit Translation (offline)
- ExoPlayer (Media3) for video
- Room Database for history
- Navigation Component
- Material Design 3
- Custom SpiralProgressView (Canvas-based)

## Color Theme
Deep Space: #06070F background, #7857FF primary, #00D4FF accent

## License
MIT
