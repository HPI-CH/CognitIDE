# CognitIDE - Physiological Studies

<!-- Plugin description -->
CognitIDE integrates body sensor hardware to record physiological data while reviewing code and generates heatmaps for assessment.

## How-To
1. To start a basic study, select **CognitIDE | Setup New Participant**.

2. After that is completed choose **CognitIDE | Start Recording**.

If needed, you can configure periodic interrupts: check **Settings/Preferences | CognitIDE Settings |
Recording | Interrupt user**.

## Features
- record data from gaze and pupil diameter data from
[Tobii Pro Nano](https://www.tobii.com/products/eye-trackers/screen-based/tobii-pro-nano).
- record physiological data from Shimmer and Emotiv devices (easily extensible for other devices supported by [LSL](https://labstreaminglayer.readthedocs.io/info/supported_devices.html))
- gather questionnaire data of subject before recording
- interrupt subject whilst recording to ask for input
- export data
  - raw data
  - element gaze hits (how often each element has been hit by a gaze sample)
  - editor screenshots (how it was edited and with heatmap highlights if applied)

It was implemented by Malte Stellmacher and Fabian Stolp.

<!-- Plugin description end -->

## Export
When saving a recording, 4 types of data will be saved (to your home directory under cognitide-recordings per default):

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

### measurements.json
Array of recorded physiological data in json format.

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
    },
    ...
  },
  ...
]
```

### elements.json
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

### interrupts.json
If enabled, contains an array of each interaction when the user was interrupted by the modal window:

```
[
  {
    "epochMillisStart": <timestamp the window appeared>,
    "epochMillisEnd": <timestamp the window was closed>,
    "answer": <optional if the user input something>
  },
  ...
]

```

### /files
This folder contains a screenshot of the editor for each file contained in the recording in png format.

## Installation

- Using IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "CognitIDE"</kbd> >
  <kbd>Install Plugin</kbd>

- Manually:

  Download the [latest release](https://github.com/HPI-CH/CognitIDE/releases) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>