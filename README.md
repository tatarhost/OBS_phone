# OBS Phone

An original Android-first screen recording studio inspired by the scene-based workflow of desktop broadcasting software. This repository is an independent implementation and does not copy OBS Studio source code or assets.

## Current status

**0.1.0 — early MVP**

- Android-native recording UI
- System screen capture using Android MediaProjection consent
- Microphone audio capture
- H.264 video + AAC audio in MP4
- Foreground recording notification
- GitHub Actions APK builds

Not implemented yet: scene compositor, camera source, desktop-style audio mixer, RTMP streaming, overlays, and advanced encoder controls. This is an early foundation, not feature parity with desktop OBS.

## Build

Requires JDK 17, Android SDK platform 35 and Android build tools 35.

```bash
gradle assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## Install

Download the APK from the Actions artifact or the Releases page, then install it on an Android device. Android will ask for screen-capture permission before each recording session.

## Output

Recordings are stored in the app-specific Movies directory under `OBSPhone`. They are private to the app sandbox in this first MVP.

## Privacy

Screen and microphone capture start only after the user initiates recording and grants Android's system permission. The app does not upload recordings.

## License

GPL-3.0. See [LICENSE](LICENSE).
