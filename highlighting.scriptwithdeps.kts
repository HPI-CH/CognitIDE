import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlinx.serialization.Serializable
import java.awt.Color
import java.awt.Point
import java.lang.Math.pow
import kotlin.math.pow


fun changeWeights(elements: Map<LookElement, Double>, measurements: List<GazeSnapshot>): Map<LookElement, Double> {

    // add your code for influencing the highlighting here
    // you can use the physiological measurements in "measurements" for that
    //
    // in the toy example below you can see how the highlighting value is altered by decreasing it the longer the
    // symbol string is (startOffset is dependent on the editor)

    val elementsAltered = elements.toMutableMap()
    elements.forEach { element -> measurements.forEach { measurement ->
        if (element.key.startOffset == measurement.lookElement?.startOffset)
            elementsAltered[element.key] = element.value / (pow(measurement.lookElement.text.length.toDouble(), 5.0))
    } }

    return elements // elementsAltered
}


fun main() {
    val json = Json {
        allowSpecialFloatingPointValues = true
    }
    val saveFolderPath: String = args.toString()
    val saveFolder = File(saveFolderPath)
    try {
        val elementsStrings =
            json.decodeFromString<Map<String, Double>>(
                File(
                    saveFolder,
                    "lookElementGazeMap.json"
                ).readText(Charsets.UTF_8)
            )

        val measurementsString =
            json.decodeFromString<List<String>>(File(saveFolder, "measurements.json").readText(Charsets.UTF_8))

        var elements = elementsStrings.mapKeys { stringToLookElement(it.key) }.toMap()
        val measurements = measurementsString.map { stringToGazeSnapshot(it) }.toList()

        elements = changeWeights(elements, measurements)

        val file = File(saveFolder, "lookElementGazeMapAlteredByUser.json")
        file.createNewFile()
        file.writeText(json.encodeToString<Map<String, Double>>(elements.mapKeys { lookElementToString(it.key) }
            .toMap()))

    } catch (ex: Exception) {
        println("EXCEPTION: " + ex)
    }
}


// Utilities

@Serializable
data class LookElement(
    val text: String,
    val filePath: String,
    val startOffset: Int
) {
    val endOffset: Int
        get() = startOffset + text.length
}


@Serializable
data class GazeSnapshot(
    val epochMillis: Long,
    val lookElement: LookElement?,
    val rawGazeData: GazeData?,
    val rawShimmerData: ShimmerData?,
    val emotivPerformanceData: EmotivPerformanceData?
)


@Serializable
data class GazeData(
    val leftEyeX: Int,
    val leftEyeY: Int,
    val rightEyeX: Int,
    val rightEyeY: Int,
    val leftPupil: Double,
    val rightPupil: Double
) {
    constructor(leftEye: Point, rightEye: Point, leftPupil: Double, rightPupil: Double) : this(
        leftEye.x,
        leftEye.y,
        rightEye.x,
        rightEye.y,
        leftPupil,
        rightPupil
    )

    val eyeCenter: Point
        get() = Point(
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


@Serializable
data class ShimmerData(
    val LOW_NOISE_ACCELEROMETER_X: Double,
    val LOW_NOISE_ACCELEROMETER_Y: Double,
    val LOW_NOISE_ACCELEROMETER_Z: Double,
    val WIDE_RANGE_ACCELEROMETER_X: Double,
    val WIDE_RANGE_ACCELEROMETER_Y: Double,
    val WIDE_RANGE_ACCELEROMETER_Z: Double,
    val MAGNETOMETER_X: Double,
    val MAGNETOMETER_Y: Double,
    val MAGNETOMETER_Z: Double,
    val GYROSCOPE_X: Double,
    val GYROSCOPE_Y: Double,
    val GYROSCOPE_Z: Double,
    val GSR: Double,
    val GSR_CONDUCTANCE: Double,
    val INTERNAL_ADC_A13: Double,
    val PRESSURE: Double,
    val TEMPERATURE: Double
)


@Serializable
data class EmotivPerformanceData(
    val value: Double,
    val attention: Double,
    val engagement: Double,
    val excitement: Double,
    val interest: Double,
    val relaxation: Double,
    val stress: Double
)


fun lookElementToString(value: LookElement): String {
    val uniqueRepresentation = "${value.text},;|${value.filePath},;|${value.startOffset}\""
    return uniqueRepresentation
}

fun stringToLookElement(lookElementString: String): LookElement {
    val parts = lookElementString.split(",;|")
    return LookElement(
        text = parts[0],
        filePath = parts[1],
        startOffset = parts[2].replace("\\", "").trim('"').toInt()
    )
}

fun stringToGazeSnapshot(gazeSnapshotString: String): GazeSnapshot {
    val parts = gazeSnapshotString.split("|;,")
    return GazeSnapshot(
        epochMillis = parts[0].replace("\\", "").trim('"').toLong(),
        lookElement = stringToLookElement(parts[1]),
        rawGazeData = stringToRawGazeData(parts[2]),
        rawShimmerData = stringToShimmerData(parts[3]),
        emotivPerformanceData = stringToEmotivPerformanceData(parts[4])
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
        rightPupil = parts[5].replace("\\", "").trim('"').toDouble()
    )
}

fun stringToShimmerData(shimmerString: String): ShimmerData? {
    val parts = shimmerString.split(",;|")
    return ShimmerData(
        LOW_NOISE_ACCELEROMETER_X = parts[0].replace("\\", "").trim('"').toDouble(),
        LOW_NOISE_ACCELEROMETER_Y = parts[1].replace("\\", "").trim('"').toDouble(),
        LOW_NOISE_ACCELEROMETER_Z = parts[2].replace("\\", "").trim('"').toDouble(),
        WIDE_RANGE_ACCELEROMETER_X = parts[3].replace("\\", "").trim('"').toDouble(),
        WIDE_RANGE_ACCELEROMETER_Y = parts[4].replace("\\", "").trim('"').toDouble(),
        WIDE_RANGE_ACCELEROMETER_Z = parts[5].replace("\\", "").trim('"').toDouble(),
        MAGNETOMETER_X = parts[6].replace("\\", "").trim('"').toDouble(),
        MAGNETOMETER_Y = parts[7].replace("\\", "").trim('"').toDouble(),
        MAGNETOMETER_Z = parts[8].replace("\\", "").trim('"').toDouble(),
        GYROSCOPE_X = parts[9].replace("\\", "").trim('"').toDouble(),
        GYROSCOPE_Y = parts[10].replace("\\", "").trim('"').toDouble(),
        GYROSCOPE_Z = parts[11].replace("\\", "").trim('"').toDouble(),
        GSR = parts[12].replace("\\", "").trim('"').toDouble(),
        GSR_CONDUCTANCE = parts[13].replace("\\", "").trim('"').toDouble(),
        INTERNAL_ADC_A13 = parts[14].replace("\\", "").trim('"').toDouble(),
        PRESSURE = parts[15].replace("\\", "").trim('"').toDouble(),
        TEMPERATURE = parts[16].replace("\\", "").trim('"').toDouble()
    )
}

fun stringToEmotivPerformanceData(emotivPerformanceDataString: String): Highlighting_scriptwithdeps.EmotivPerformanceData? {
    val parts = emotivPerformanceDataString.split(",;|")
    return EmotivPerformanceData(
        value = parts[0].replace("\\", "").trim('"').toDouble(),
        attention = parts[1].replace("\\", "").trim('"').toDouble(),
        engagement = parts[2].replace("\\", "").trim('"').toDouble(),
        excitement = parts[3].replace("\\", "").trim('"').toDouble(),
        interest = parts[4].replace("\\", "").trim('"').toDouble(),
        relaxation = parts[5].replace("\\", "").trim('"').toDouble(),
        stress = parts[6].replace("\\", "").trim('"').toDouble()
    )
}


main()