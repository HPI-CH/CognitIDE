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
import java.awt.geom.Arc2D
import java.lang.Math.pow
import kotlin.math.pow


fun changeWeights(elements: Map<LookElement, Double>, measurements: List<GazeSnapshot>): Map<LookElement, Double> {

    // add your code for influencing the highlighting here
    // you can use the physiological measurements in "measurements" for that
    //
    // in the toy example below you can see how the highlighting value is altered by decreasing it the longer the
    // symbol string is (startOffset is dependent on the editor)

    var elementsAltered = elements.mapValues { 0.0 }.toMutableMap()
    val elementsCounter = elements.mapValues { 0.0 }.toMutableMap()
    elements.forEach { element -> measurements.forEach { measurement ->
        if (element.key.startOffset == measurement.lookElement?.startOffset) {
                elementsAltered[element.key] = elementsAltered[element.key]!! + measurement.otherLSLData
                    .filterIndexed { index, _ -> index % 2 != 0 }
                    .mapNotNull { it?.getOrNull(2)?.takeIf { it != -999f } }
                    .let { if (it.isEmpty()) 0.0 else it.average().also { elementsCounter[element.key] = elementsCounter[element.key]!! + 1.0 } }
        }
    } }
    elementsAltered = elementsAltered.mapValues { it.value / elementsCounter[it.key]!! } as MutableMap<LookElement, Double>

    return elementsAltered
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
data class FloatArrayContainer(val data: List<FloatArray?>)


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
    val otherLSLData: List<FloatArray?>
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
        otherLSLData = stringToOtherLSLData(parts[3])
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

fun stringToOtherLSLData(dataString: String): List<FloatArray?> {
    val floatListsString = dataString.split("\":")[1].trim('}', '"').split("],[")
    if ("[]" in floatListsString) return emptyList()

    val transformedList = floatListsString.map {
        it.trim('[', ']').split(",").map { numStr -> numStr.trim().toFloat() }.toFloatArray()
    }

    return transformedList
}

main()