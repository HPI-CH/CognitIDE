CognitIDE
=========
**An IDE Plugin for Facilitating Physiological Assessments in Software Engineering Studies**

<!-- Plugin description -->
CognitIDE integrates body sensor hardware to record physiological data while reading and interacting with code and
generates code highlightings and heatmaps for assessment.
It is available for IntelliJ-based IDEs with build numbers 221 to 222. Lab Streaming Layer (LSL) streams are supported.

## Installation

Download the [latest release](https://github.com/HPI-CH/CognitIDE/releases) and install it manually using
<kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Features

- Record gaze and pupil diameter data from [Tobii Pro Eye Trackers](https://www.tobii.com/products/eye-trackers/).
- Record physiological data
  from [LSL-supported devices](https://labstreaminglayer.readthedocs.io/info/supported_devices.html).
- Map gathered sensor data to code position using the gaze information.
- Preview customizable questionnaires within the plugin.
- Configure an individual Study Workflow to automatically execute various events like  :
    - showing explanatory pop-ups
    - opening videos or sounds (e.g. for baseline recordings) using an external media player
    - opening files and codebases (with tasks or tests)
    - opening questionnaires (e.g. before, after or in between tasks)
- Export recorded data with:
    - participant information
    - raw data of physiological measurements with timestamps
    - element gaze hits (how often, when and how direct each code element has been hit by a gaze sample)
    - file data storing:
        - initial file contents
        - changes per file and timepoint (in detail: collecting all changes in a short timeframe together as a set)
    - highlighting intensity per code element
    - editor screenshots (how it was edited and with heatmap highlights if applied)
- Highlight and reconstruct the files used in a recording.

It was implemented by Franziska Hradilak, Lara Kursawe, Magnus Menger,
Franz Sauerwald, Stefan Spangenberg, Malte Stellmacher, Fabian Stolp.

<!-- Plugin description end -->

## Quickstart
This section explains how to do a first test recording.
It will use the mouse to simulate the gaze and visualize the code elements, the mouse hovered over.

0. (You might need to specify a highlighting command, before you can execute the following steps. For more info see the corresponding section below).

1. To start a basic study, select <kbd>CognitIDE</kbd> > <kbd>Setup New Participant</kbd>.

2. After that is completed choose <kbd>CognitIDE</kbd> > <kbd>Start Recording</kbd>.

3. You can stop the recording with <kdb>CognitIDE</kdb> > <kdb>Stop Recording</kdb> and then save it with <kdb>CognitIDE</kdb> > <kdb>Save Recording</kdb>.

4. You can visualize the recording inside the code with <kdb>CognitIDE</kdb> > <kdb>Visualize Last Saved Recording</kdb>.

5. For a recording with more sensors, automated studies, custom questionaires or custom highlighting scripts, read the following sections.

## Configuring Sensors

Before starting a recording, the LSL streams for all sensors have to be configured in the settings. The settings of
CognitIDE can be opened by first [opening the IntelliJ settings](https://www.jetbrains.com/help/idea/2022.1/configuring-project-and-ide-settings.html),
then scrolling to `CognitIDE Settings` on the left side. There in the Device Stream section, the sensors can be configured.
With the Gaze Recording Source dropdown, the source of the position data for mapping all other physiological data can be
chosen. One of Mouse and Tobii can be selected.

Below, any number of device streams can be added using the Add Stream button. For each stream, the stream name has to 
match the name of the LSL stream so that the plugin can identify the stream. Optionally, a Connector application path can
be configured, which is currently unused, but can be used in the future to start a connector application (creating the 
LSL stream for that sensor) automatically.

## Automatic Study Workflow

You can automatically conduct a study within the plugin by configuring a Study Workflow. The Study Workflow consists of
a list of actions, which are executed sequentially by IntelliJ. It allows you to start and stop recordings, open
questionnaires and files or display popups automatically. Actions are built using so called Workflow Items.

A workflow can be imported via JSON, an example JSON with all types of possible Workflow Items can be found
in `resources/Workflows/Example_workflow.json`. To add an action, which does not have its own Workflow Item,
use the generic `WorkflowItem`:

```
{
    "type": "com.github.hpich.cognitide.services.study.WorkflowItem",
    "enabled": true,
    "actionId": "com.github.hpich.cognitide.actions.DesiredAction"
}
```

1. Open <kbd>File</kbd> > <kbd>Settings</kbd> > <kbd>CognitIDE Settings</kbd> and in the <kbd>Study
   Workflow</kbd> section, select the
   desired JSON file.
2. Select <kbd>CognitIDE</kbd> > <kbd>Start Study Workflow</kbd> to begin.
3. Select <kbd>CognitIDE</kbd> > <kbd>Next Workflow Step</kbd> to skip to the next action.
4. The Study Workflow either ends, after the last action was executed or by pressing <kbd>CognitIDE</kbd> > <kbd>Stop
   Study Workflow and Save Data</kbd>,
   in which case any ongoing recordings with be stopped and saved.

## Configuring Questionnaires

You can display personalized questionnaires within the plugin, either by themselves or as part of a Study Workflow.
A questionnaire can be imported as JSON, some examples can be found in `resources/Questionnaires`.
Currently, four different question types are supported, each expecting slightly different values:

- Dropdown:

```
{
  "title": "Question Title",
  "property": "unique_id",
  "type": "dropdown",
  "answers": ["answer_1", "answer_2", "answer_3"]
}
```

- Freetext

```
{
  "title": "Question Title",
  "property": "unique_id",
  "type": "freetext"
}
```

- Slider

```
{
  "title": "Question Title",
  "property": "unique_id",
  "type": "slider",
  "min": 0,
  "max": 10,
  "minorTickSpacing": 1,
  "majorTickSpacing": 5
}
```

- Number

```
{
  "title": "Question Title",
  "property": "unique_id",
  "type": "number",
  "min": 1,
  "max": 10
}
```

### Select and View Questionnaire

To select and view a questionnaire, select <kbd>CognitIDE</kbd> > <kbd>Select and View Questionnaire</kbd>.
A path selector will open, and you can find and select the desired questionnaire.

### Show Questionnaire in Study Workflow

To view a questionnaire as part of a Study Workflow, use a `QuestionnaireWorkflowItem`:

```
  {
    "type": "com.github.hpich.cognitide.services.study.QuestionnaireWorkflowItem",
    "enabled": true,
    "questionnaireFilePath": "C:\\Users\\Example-User\\Desktop\\example_questionnaire.json",
    "questionnaireName": "example"
  }
```


## Configuring the Highlighting Script

To be able to visualize the recorded data inside the source code, you will need to specify a highlighting command.
This command can be used to execute a highlighting script.
A simple example for such a highlighting script is `highlighting.py` inside this repository.
To use this script, go to IntelliJSettings and set `CognitIDE Settings` > `HighlightingConfiguration` > `Command:` to `python "<path/to/highlighting.py>"`.
The visualizing a recording, the highlighting command will be appended with ` <path/to/recording/folder> <timestamp>` and executed.
This allows you to write custom scripts or programs that can read the recording folder, calculate a custom highlighting
for the data up to the specified timestamp and output the calculated values into a file called `higlighting.json`.
The structure of the recording files and the highlighting file are explained below.


## Export

When saving a recording, seven types of data will be saved (to your home directory under cognitide-recordings per
default):

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

### sensorData

```
{
  Sensor Name: <string>:
  [
    {"time":<timestamp>,
    "values":[value,...],
    ...}
  ],
  ...
}
```

### initialFileContents

```
{
  "path": <code file path>:
  {
    "text": <file content>,
    "elementOffsets:
    {
      "id":{
        "first":<integer>,
        "second":<integer>
      }
    }  
  },
  ...
  
}
```

### highlighting

```
{
  "id"<integer> : <highlighting intensity>
  ...
  }
```

### gazeData

```
{
  "id": <integer, code element id>,
  [{"time": < float>, "weight": < gaze intensity to code element at time t>},
  ...],
  ...
}
```

### fileChangeData

```
{
  "path": <path to code file>
  :[
    {"startTime: <timestamp>,
     "endTime": <timestamp>,
     changes: [
       {"time":<timestamp>,
       "oldText": <string>,
       "newText": <string>},
       ...
     ] 
     },
     ...
  ]
}
```

### /files

This folder contains a screenshot of the editor for each file contained in the recording in PNG format.

## Development

### Linter

Run ktlint to validate the style of all Kotlin files

```sh
./gradlew ktlintCheck
```

Format files to correct most style violations automatically

```sh
./gradlew ktlintFormat
```
