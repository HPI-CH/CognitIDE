# DataCollectingService

Defined in `plugin.xml` the `DataCollecting` service is a singleton instance of the central data holder.
Notable it contains the three data fields:

| Store                | Type                                                                                                                                                                                                              |
|----------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `gazeSnapshotList`   | A time-series list of snapshots taken during a recording. A gaze snapshot includes a timestamp of the arrival time and optionally data from connected devices. Also a `LookElement` might be saved if applicable. |
| `lookElementGazeMap` | Map of code elements (`LookElement`, identified by their encapsulating file and offset inside is this file) to their highlight value.                                                                             |
| `userInterruptList`  | A list of user-entered input when interrupted by the plugin during a recording. Configurable inside the the Settings configurable.                                                                                |
## StudyRecorder

Inside is also the `currentRecorder`. This is an instance of the current recorder used to record data from external devices. In its simplest form (like `MouseGazeRecorder`) a *recorder* records from a single devices and writes the data to its parent *data collecting service*. It is however structured to also record from multiple devices simultaneously. A Subclass has to implement three methods:

| Method    | Behaviour                                                                                                                                                                                                         |
|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `setup`   | Before starting a recording this function is called to optionally setup required data structures or establish a connecting to external devices. Should return `true` when successful.                             |
| `dispose` | When the recording is stopped this method is called to cleanup and close remaining connections                                                                                                                    |
| `loop`    | Called periodically (with a configurable delay of `delay` milliseconds from the completion of the last loop. Here data should be retrieved from incoming buffers and stored in the parent `dataCollectingService` |


# Recording new device types

For a device to be supported, it has to have its
own [StudyRecorder](https://github.com/HPI-CH/CognitIDE/blob/main/src/main/kotlin/com/github/hpich/cognitide/services/StudyRecorder.kt)
subclass.
A good starting point to look into is the `MouseGazeRecorder`.

## A generic catch-all LSL recorder
> Note: The current state enforces that each recorder has to parse the data of the incoming LSL sample. This is especially true for the eye-tracker as its coordinates need to be interpreted the correct way to map it to potential code elements while the program is running.
> 
> For other devices that are just recording e.g. vital data points this is not necessarily needed. An option would be to have a generic LSL recorder that does not parse the samples it is receiving but just attaching it to the current gaze sample. A researcher could edit the corresponding kotlin-script on-the-fly to map this devices data by index to a real-valued value.