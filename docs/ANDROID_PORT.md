# Android OBS port — engineering plan

## Goal

Port the real OBS Studio engine to Android and build a mobile control surface around it. Reusing only the OBS name or drawing a similar UI is not sufficient.

## Upstream facts

- Upstream source: https://github.com/obsproject/obs-studio
- OBS backend architecture is based on libobs: https://obs-docs.rodney.io/backend-design
- The current upstream libobs/CMakeLists.txt has platform branches for Windows, macOS, Linux, FreeBSD, and OpenBSD, but no Android branch.
- The top-level upstream build also adds desktop frontend and platform-specific modules. The Qt desktop frontend is not the intended Android UI.

This means Android support needs real platform work. A successful Gradle build of the current Java recorder does not demonstrate that OBS itself has been ported.

## Required work, in dependency order

1. **Pin and vendor upstream OBS source correctly**
   - Clone upstream recursively at a fixed release/tag or commit.
   - Preserve upstream copyright headers, COPYING, and third-party license notices.
   - Record the exact upstream revision in the build metadata.

2. **Make a minimal Android libobs build**
   - Add Android detection and platform abstractions where needed.
   - Resolve FFmpeg, zlib, jansson, uthash, SIMDe, libcaption, and other dependencies for Android ABIs.
   - Provide Android-compatible threading, filesystem, logging, and dynamic-module loading.
   - Implement or adapt a graphics backend suitable for Android/EGL/GLES/Vulkan as selected by the port.
   - Build and load a real shared libobs library from an NDK toolchain.

3. **JNI lifecycle bridge**
   - Initialize/shutdown OBS safely from Android lifecycle events.
   - Expose scenes, sources, outputs, encoders, and audio controls through a narrow JNI API.
   - Keep all long-running engine work off the UI thread.

4. **Android OBS sources**
   - Implement a MediaProjection-backed video source feeding OBS video frames.
   - Implement microphone and supported device-audio sources through Android audio APIs.
   - Add camera and image/media sources after the core source pipeline works.

5. **Mobile frontend**
   - Touch-first scene/source list and preview.
   - Audio mixer with meters and mute controls.
   - Scene transitions, recording, streaming destination setup, and encoder settings.
   - Respect Android permission prompts, foreground-service rules, rotation, thermal limits, and device memory.

6. **Verification**
   - First prove libobs initializes on a real ARM64 Android device.
   - Then prove one Android capture source renders through an OBS scene.
   - Then verify audio mixing and recording through OBS outputs.
   - Only then publish an APK as an OBS-engine preview.

## Current status

This branch updates project identity and licensing documentation only. It does not yet compile or bundle libobs. The old v0.1.0 APK remains a separate recorder prototype.
