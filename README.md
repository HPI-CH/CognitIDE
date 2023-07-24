# OpenEye - Eye-Tracking Studies

![Build](https://github.com/DieKautz/ide-plugin/workflows/Build/badge.svg?style=flat-square)
[![Version](https://img.shields.io/jetbrains/plugin/v/22291.svg?style=flat-square)](https://plugins.jetbrains.com/plugin/22291)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/22291.svg?style=flat-square)](https://plugins.jetbrains.com/plugin/22291)

<!-- Plugin description -->
OpenEye integrates eye tracking hardware to record gaze data while reviewing code and generate heatmaps for assessment.

## How-To
1. To start a basic study, select **OpenEye | Setup New Participant**.

2. After that is completed choose **OpenEye | Start Tobii Pro Recording**.

If needed, you can configure periodic interrupts: check **Settings/Preferences | OpenEye Settings |
Recording | Interrupt user**.

## Features
- record data from gaze and pupil diameter data from
[Tobii Pro Nano](https://www.tobii.com/products/eye-trackers/screen-based/tobii-pro-nano).
- gather basic questioner data of subject before recording
- interrupt subject whilst recording to ask for input
- export data
  - raw data
  - element gaze hits (how often each element has been hit by a gaze sample)
  - editor screenshots (how it was edited and with heatmap highlights if applied)

It was build for my bachelors' thesis to assess code complexity using eye tracker gaze data and by that 
pinpointing gaze to specific [psi elements](https://plugins.jetbrains.com/docs/intellij/psi-elements.html).

<!-- Plugin description end -->

## Export
When saving a recording, 4 types of data will be saved (to your home directory under openeye-recordings per default):

### participant.json
Basic information provided by the participant questioner in json format.

```
{
  "id": <4-digit random integer>,
  "horizontalSpread": <horizontal inaccuracy margin in px>,
  "verticalSpread": <vertical inaccuracy margin in px>,
  ...
}
```

### gaze.json
Array of recorded gaze elements in json format.

```
[
  {
    "epochMillis": <recording time>,
    "lookElement": {
      "text": <display text of psi element>,
      "filePath": <file path of source file>,
      "startOffset": <offset in file where psi element starts>
    },
    "rawGazeData": {
      "leftEyeX": <screen-space coordinate>,
      "leftEyeY": <screen-space coordinate>,
      "rightEyeX": <screen-space coordinate>,
      "rightEyeY": <screen-space coordinate>,
      "leftPupil": <pupil diameter>,
      "rightPupil": <pupil diameter>
    }
  },
  ...
]
```

### gaze.json
Array of how many times an element was hit by gaze data in json format.

```
[
  {
    "lookElement": {
      "text": <display text of psi element>,
      "filePath": <file path of source file>,
      "startOffset": <offset in file where psi element starts>
    },
    "gazeWeight": <fractional value>
  },
  ...
]
```

### /files
This folder contains a screenshot of the editor for each file contained in the recording in png format.

## Installation

- Using IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "OpenEye"</kbd> >
  <kbd>Install Plugin</kbd>

- Manually:

  Download the [latest release](https://github.com/DieKautz/ide-plugin/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>
