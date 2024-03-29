<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# OpenEye Changelog

## [Unreleased]

### Fixed
- Ensure tobii correct stream channel count
- Fixed lsl library in sub-folder not being found in marketplace version

## [1.0.0] - 2023-07-25

### Added
- Added missing questioner for project size
- Added option to adjust inaccuracy margin of a specific setup
- Added How-To section in plugin description
- Added second menu entry for debug actions
- Added saving of an image of all editors when saving recording data (must open them before and highlight)
- Interrupt user and prompt for answer/code while recording
- Added configuration option for interrupt
- Added action for highlighting gaze elements
- Added icons for all actions
- Improved interactive participant setup
- Added configuration option for device sn, to directly open user calibration screen
- Added extra description to external programs
- Added open file action after save
- Added correction if one eye was not recognised
- Improve highlight colors readability
- Ask if recording and participant data should be cleared
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

### Fixed
- Fixed not all files opening when trying to open all recorded files at once
- Fixed label alignment in questioner
- Fixed conversion of screen-space coordinates to editor-space when using eye tracker
- Fixed recorder still running after failed setup
- Fixed interrupts appearing after recording was stopped
- Fixed mouse location on windows when multiple files are opened simultaneously
- Default save location is now user.home/openeye-recordings instead of fs root
- Calls to deprecated api
- Elements being highlighted in wrong editor

### Removed
- Wizard no longer starts recording in last step
- Removed deprecated debug actions
- Removed unused tool window

## [1.0.0-rc.4] - 2023-07-24

### Added
- Added option to adjust inaccuracy margin of a specific setup
- Added How-To section in plugin description
- Added second menu entry for debug actions
- Added saving of an image of all editors when saving recording data (must open them before and highlight)
- Interrupt user and prompt for answer/code while recording
- Added configuration option for interrupt
- Added action for highlighting gaze elements
- Added icons for all actions
- Improved interactive participant setup
- Added configuration option for device sn, to directly open user calibration screen
- Added extra description to external programs
- Added open file action after save
- Added correction if one eye was not recognised
- Improve highlight colors readability
- Ask if recording and participant data should be cleared
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

### Fixed
- Fixed conversion of screen-space coordinates to editor-space when using eye tracker
- Fixed recorder still running after failed setup
- Fixed interrupts appearing after recording was stopped
- Fixed mouse location on windows when multiple files are opened simultaneously
- Default save location is now user.home/openeye-recordings instead of fs root
- Calls to deprecated api
- Elements being highlighted in wrong editor

### Removed
- Wizard no longer starts recording in last step
- Removed deprecated debug actions
- Removed unused tool window

## [1.0.0-rc.3] - 2023-07-22

### Added
- Added How-To section in plugin description
- Added second menu entry for debug actions
- Added saving of an image of all editors when saving recording data (must open them before and highlight)
- Interrupt user and prompt for answer/code while recording
- Added configuration option for interrupt
- Added action for highlighting gaze elements
- Added icons for all actions
- Improved interactive participant setup
- Added configuration option for device sn, to directly open user calibration screen
- Added extra description to external programs
- Added open file action after save
- Added correction if one eye was not recognised
- Improve highlight colors readability
- Ask if recording and participant data should be cleared
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

### Fixed
- Default save location is now user.home/openeye-recordings instead of fs root
- Calls to deprecated api
- Elements being highlighted in wrong editor

### Removed
- Wizard no longer starts recording in last step
- Removed deprecated debug actions
- Removed unused tool window

## [1.0.0-rc.2] - 2023-07-18

### Added
- Ask if recording and participant data should be cleared
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

## [0.0.2]

### Added
- Consider all opened editors not just the one with keyboard focus

## [0.0.1]

### Added
- Export for raw gaze data along with time-synchronized file or editor focus
- Export for gaze counters of elements
- Visualization feature for recorded gaze
- Support for Tobii Pro Nano through [labstreaminglayer/App-TobiiPro](https://github.com/labstreaminglayer/App-TobiiPro)
- Debug mouse cursor recording mode to simulate an eye tracking device with 10Hz
- Initial scaffold created
  from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)

[Unreleased]: https://github.com/DieKautz/ide-plugin/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/DieKautz/ide-plugin/compare/v1.0.0-rc.4...v1.0.0
[1.0.0-rc.4]: https://github.com/DieKautz/ide-plugin/compare/v1.0.0-rc.3...v1.0.0-rc.4
[1.0.0-rc.3]: https://github.com/DieKautz/ide-plugin/compare/v1.0.0-rc.2...v1.0.0-rc.3
[1.0.0-rc.2]: https://github.com/DieKautz/ide-plugin/compare/v0.0.2...v1.0.0-rc.2
[0.0.2]: https://github.com/DieKautz/ide-plugin/compare/v0.0.1...v0.0.2
[0.0.1]: https://github.com/DieKautz/ide-plugin/commits/v0.0.1
