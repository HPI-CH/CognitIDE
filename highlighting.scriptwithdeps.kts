import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.Point
import java.io.File
import java.util.*


/**
 * Adjusts the weights of look elements based on sensor measurements.
 *
 * This function modifies the highlighting values of each look element based on sensor measurements provided in the gaze
 * snapshots. The function can be adjusted by the user depending on what data they would like the highlighting to depend
 * on. In the example below, the highlighting has been set so that it depends on acceleration measurements.
 *
 * @param elements A map of LookElement to default weights (how long a position has been viewed).
 * @param measurements A list of GazeSnapshot containing sensor measurements.
 * @return A map of LookElement with updated weights.
 */
fun adjustLookElementWeights(
    elements: Map<LookElement, Double>,
    measurements: List<GazeSnapshot>,
): Map<LookElement, Double> {
    @Suppress("ktlint")
    val RETURN_DEFAULT_WEIGHTS = true

    @Suppress("ktlint")
    val SHIMMER_ID = 2
    @Suppress("ktlint")
    val NUMBER_OF_STREAMS = 2
    @Suppress("ktlint")
    val Z_AXIS_ACCELERATION_ID = 2

    if (RETURN_DEFAULT_WEIGHTS) return elements

    val alteredElements = mutableMapOf<LookElement, Double>().apply { elements.forEach { put(it.key, 0.0) } }
    val elementCounters = mutableMapOf<LookElement, Double>().apply { elements.forEach { put(it.key, 0.0) } }

    elements.forEach { (element, _) ->
        measurements.forEach { measurement ->
            if (element.startOffset == measurement.lookElement?.startOffset) {
                val averageSensorValues =
                    calculateSensorValuesAverage(
                        measurement,
                        streamID = SHIMMER_ID,
                        numberOfStreams = NUMBER_OF_STREAMS,
                        channelID = Z_AXIS_ACCELERATION_ID,
                    )
                if (averageSensorValues != null) {
                    alteredElements[element] = alteredElements[element]!! + averageSensorValues
                    elementCounters[element] = elementCounters[element]!! + 1.0
                }
            }
        }
    }

    return alteredElements.mapValues {
        if (elementCounters[it.key]!! > 0) it.value / elementCounters[it.key]!! else it.value
    }
}

/**
 * Calculates the average for a stream channel from a GazeSnapshot.
 *
 * @param measurement The GazeSnapshot containing sensor data.
 * @param streamID The LSL stream of interest.
 * @param numberOfStreams The overall number of recorded LSL streams.
 * @param channelID The stream channel of interest.
 * @return The average sensor value or null if no valid data is available.
 */
fun calculateSensorValuesAverage(
    measurement: GazeSnapshot,
    streamID: Int,
    numberOfStreams: Int,
    channelID: Int,
): Double? {
    val sensorValues =
        measurement.otherLSLData.filterIndexed { index, _ -> ((index + 1) - streamID) % numberOfStreams == 0 }
            .mapNotNull { it?.getOrNull(channelID) }

    return if (sensorValues.isNotEmpty()) sensorValues.average() else null
}

fun main() {
    val json =
        Json {
            allowSpecialFloatingPointValues = true
        }
    val saveFolderPath: String = args.toString()
    val saveFolder = File(saveFolderPath)
    try {
        val elementsStrings =
            json.decodeFromString<Map<String, Double>>(
                File(
                    saveFolder,
                    "lookElementGazeMap.json",
                ).readText(Charsets.UTF_8),
            )

        val measurementsString =
            json.decodeFromString<List<String>>(File(saveFolder, "measurements.json").readText(Charsets.UTF_8))

        var elements = elementsStrings.mapKeys { stringToLookElement(it.key) }.toMap()
        val measurements = measurementsString.map { stringToGazeSnapshot(it) }.toList()

        elements = adjustLookElementWeights(elements, measurements)

        val file = File(saveFolder, "lookElementGazeMapAlteredByUser.json")
        file.createNewFile()
        file.writeText(
            json.encodeToString<Map<String, Double>>(
                elements.mapKeys { lookElementToString(it.key) }
                    .toMap(),
            ),
        )
    } catch (ex: Exception) {
        println("EXCEPTION: " + ex)
    }
}

// Utilities
@Serializable
data class FloatArrayContainer(val data: List<FloatArray?>)

@Serializable
data class LookElement(
    val text: String,
    val filePath: String,
    val startOffset: Int,
) {
    val endOffset: Int
        get() = startOffset + text.length
}

@Serializable
data class GazeSnapshot(
    val epochMillis: Long,
    val lookElement: LookElement?,
    val rawGazeData: GazeData?,
    val otherLSLData: List<FloatArray?>,
)

@Serializable
data class GazeData(
    val leftEyeX: Int,
    val leftEyeY: Int,
    val rightEyeX: Int,
    val rightEyeY: Int,
    val leftPupil: Double,
    val rightPupil: Double,
) {
    constructor(leftEye: Point, rightEye: Point, leftPupil: Double, rightPupil: Double) : this(
        leftEye.x,
        leftEye.y,
        rightEye.x,
        rightEye.y,
        leftPupil,
        rightPupil,
    )

    val eyeCenter: Point
        get() =
            Point(
                (leftEyeX + rightEyeX) / 2,
                (leftEyeY + rightEyeY) / 2,
            )

    fun correctMissingEye(): GazeData? {
        if (leftPupil.isNaN() && rightPupil.isNaN()) {
            return null
        }
        if (leftPupil.isNaN()) {
            return GazeData(rightEyeX, rightEyeY, rightEyeX, rightEyeY, leftPupil, rightPupil)
        }
        if (rightPupil.isNaN()) {
            return GazeData(leftEyeX, leftEyeY, leftEyeX, leftEyeY, leftPupil, rightPupil)
        }
        return this
    }
}

fun lookElementToString(value: LookElement): String {
    val uniqueRepresentation = "${value.text},;|${value.filePath},;|${value.startOffset}\""
    return uniqueRepresentation
}

fun stringToLookElement(lookElementString: String): LookElement {
    val parts = lookElementString.split(",;|")
    return LookElement(
        text = parts[0],
        filePath = parts[1],
        startOffset = parts[2].replace("\\", "").trim('"').toInt(),
    )
}

fun stringToGazeSnapshot(gazeSnapshotString: String): GazeSnapshot {
    val parts = gazeSnapshotString.split("|;,")
    return GazeSnapshot(
        epochMillis = parts[0].replace("\\", "").trim('"').toLong(),
        lookElement = stringToLookElement(parts[1]),
        rawGazeData = stringToRawGazeData(parts[2]),
        otherLSLData = stringToOtherLSLData(parts[3]),
    )
}

fun stringToRawGazeData(gazeDataString: String): GazeData? {
    val parts = gazeDataString.split(",;|")
    return GazeData(
        leftEyeX = parts[0].replace("\\", "").trim('"').toInt(),
        leftEyeY = parts[1].replace("\\", "").trim('"').toInt(),
        rightEyeX = parts[2].replace("\\", "").trim('"').toInt(),
        rightEyeY = parts[3].replace("\\", "").trim('"').toInt(),
        leftPupil = parts[4].replace("\\", "").trim('"').toDouble(),
        rightPupil = parts[5].replace("\\", "").trim('"').toDouble(),
    )
}

fun stringToOtherLSLData(dataString: String): List<FloatArray?> {
    val floatListsString = dataString.split("\":")[1].trim('}', '"').split("],[")
    if ("[]" in floatListsString) return emptyList()

    val transformedList =
        floatListsString.map {
            it.trim('[', ']').split(",").map { numStr -> numStr.trim().toFloat() }.toFloatArray()
        }

    return transformedList
}

main()
