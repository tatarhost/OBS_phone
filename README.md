# OBS Phone — Android port project

**Goal:** adapt the real OBS Studio engine for Android phones, with a touch-first interface. This repository is not the official OBS Project and is not affiliated with it.

## Important status

The existing v0.1.0 APK is only a small Android screen-recorder prototype. It does **not** contain libobs, the OBS scene system, OBS encoders/outputs, or OBS plugins. It must not be described as an OBS Studio port.

The obs-native-port branch starts the migration work. No Android APK containing the real OBS engine is available yet.

## Target architecture

- **Engine:** upstream OBS Studio / libobs from https://github.com/obsproject/obs-studio, with upstream notices and license preserved.
- **Android integration:** JNI bridge and Android-specific implementations for graphics, audio, capture, and lifecycle.
- **Capture:** Android MediaProjection source, created as an OBS source rather than a separate MediaRecorder pipeline.
- **UI:** native touch-first scene/source dock, preview, audio mixer, transitions, recording and streaming controls.
- **Outputs:** OBS output/encoder pipeline, added only after the Android engine backend is working.

## Port status

See docs/ANDROID_PORT.md for the technical plan and current blockers.

The upstream OBS CMake project currently selects Windows, macOS, Linux, FreeBSD, and OpenBSD platform implementations for libobs; Android is not a supported platform in that build configuration. Therefore simply adding the desktop source tree to an Android Gradle project will not produce a working port. Android platform glue and dependency builds are required first.

## Current APK

The existing release is a legacy recorder prototype, not OBS Studio:
- https://github.com/tatarhost/OBS_phone/releases/tag/v0.1.0

Do not use that APK as evidence that the native OBS port is complete.

## License

OBS Studio is distributed under GPL-2.0. The project license file is aligned to the upstream GPL-2.0 text for the porting work. Individual third-party dependencies and assets may have their own licenses; see their notices before redistribution.
