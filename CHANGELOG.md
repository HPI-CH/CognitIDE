<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# ide-plugin Changelog

## [Unreleased]

### Added
- Added Add global setting for recordings save location
- Save participant information alongside data
- Added SetupNewParticipantAction
- Added participant questioner
- Suggest starting tobii connector when no streams are found
- Introduce settings menu for external applications
- Added action for saving all data
- Added actions for starting and stopping recordings
- Added action for clearing highlights
- Clear all highlights on new recording or clear
- Improve interoperability of element gaze json
- Fix serialization of invalid pupil diameters
- Fix coordinate typo in Tobii recording service

## 0.0.2

### Added

- Consider all opened editors not just the one with keyboard focus

## 0.0.1

### Added

- Export for raw gaze data along with time-synchronized file or editor focus
- Export for gaze counters of elements
- Visualization feature for recorded gaze
- Support for Tobii Pro Nano through [labstreaminglayer/App-TobiiPro](https://github.com/labstreaminglayer/App-TobiiPro)
- Debug mouse cursor recording mode to simulate an eye tracking device with 10Hz
- Initial scaffold created
  from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
