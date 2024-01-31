CognitIDE
=========
**An IDE Plugin for Facilitating Physiological Assessments in Software Engineering Studies**

<!-- Plugin description -->
CognitIDE integrates body sensor hardware to record physiological data while reading and interacting with code and generates code highlightings and heatmaps for assessment.
It is available for IntelliJ-based IDEs with build numbers 221 to 222. Lab Streaming Layer (LSL) streams are supported.

## Installation

  Download the [latest release](https://github.com/HPI-CH/CognitIDE/releases) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Highlighting script
For an example for the highlighting script you could take a look at and/or download highlighting.scriptwithdeps.kts.

## How-To
1. To start a basic study, select **CognitIDE | Setup New Participant**.

2. After that is completed choose **CognitIDE | Start Recording**.

If needed, you can configure periodic interrupts: check **Settings/Preferences | CognitIDE Settings |
Recording | Interrupt user**.

## Features
- record gaze and pupil diameter data from [Tobii Pro Eye Trackers](https://www.tobii.com/products/eye-trackers/).
- record physiological data from [LSL-supported devices](https://labstreaminglayer.readthedocs.io/info/supported_devices.html)
- all gathered sensor data is mapped to code positions through the gaze information
- gather questionnaire data of the subject before recording
- interrupt the subject whilst recording to ask for input
- export data
  - raw data
  - element gaze hits (how often each element has been hit by a gaze sample)
  - editor screenshots (how it was edited and with heatmap highlights if applied)

It was implemented by Malte Stellmacher and Fabian Stolp.

<!-- Plugin description end -->

## Export
When saving a recording, four types of data will be saved (to your home directory under cognitide-recordings per default):

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
This folder contains a screenshot of the editor for each file contained in the recording in PNG format.


## DataCollectingService

Defined in `plugin.xml` the `DataCollecting` service is a singleton instance of the central data holder.
Notably, it contains three data fields:

| Store                | Type                                                                                                                                                                                                              |
|----------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `gazeSnapshotList`   | A time-series list of snapshots taken during a recording. A gaze snapshot includes a timestamp of the arrival time and optionally data from connected devices. Also a `LookElement` might be saved if applicable. |
| `lookElementGazeMap` | Map of code elements (`LookElement`, identified by their encapsulating file and offset inside is this file) to their highlight value.                                                                             |
| `userInterruptList`  | A list of user-entered input when interrupted by the plugin during a recording. Configurable inside the the Settings configurable.                                                                                |
### StudyRecorder

Inside is also the `currentRecorder`. This is an instance of the current recorder used to record data from external devices. In its simplest form (like `MouseGazeRecorder`) a *recorder* records from a single device and writes the data to its parent *data collecting service*. It is however structured to also record from multiple devices simultaneously. A Subclass has to implement three methods:

| Method    | Behaviour                                                                                                                                                                                                         |
|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `setup`   | Before starting a recording this function is called to optionally set up required data structures or establish a connection to external devices. Should return `true` when successful.                             |
| `dispose` | When the recording is stopped this method is called to cleanup and close remaining connections                                                                                                                    |
| `loop`    | Called periodically (with a configurable delay of `delay` milliseconds from the completion of the last loop. Here data should be retrieved from incoming buffers and stored in the parent `dataCollectingService` |


## Recording new device types

For a device to be supported, it has to have its
own [StudyRecorder](https://github.com/HPI-CH/CognitIDE/blob/main/src/main/kotlin/com/github/hpich/cognitide/services/StudyRecorder.kt)
subclass.
A good starting point to look into is the `MouseGazeRecorder`.

### Generic catch-all LSL recorder
For the main eye-tracker, the recorder has to parse the data of the incoming LSL sample as its coordinates need to be interpreted in the correct way to map it to potential code elements while the program is running.

For other devices that are recording e.g. vital data points this is not needed. The generic LSL recorder does not parse the samples it is receiving but just attaches it to the current gaze sample. A user can edit the corresponding [kotlin script for highlighting](https://github.com/HPI-CH/CognitIDE/blob/main/highlighting.scriptwithdeps.kts) on-the-fly to map these devices' data by index to real-valued values used for highlighting.
