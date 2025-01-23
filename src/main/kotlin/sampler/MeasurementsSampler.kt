package sampler
import java.time.LocalDateTime
enum class MeasurementType {
    TEMP, SPO2, HEART_RATE
}

data class Measurement(
    val measurementTime: LocalDateTime,
    val measurementValue: Double,
    val type: MeasurementType
)

class MeasurementSampler {

    fun sample(
        startOfSampling: LocalDateTime,
        unsampledMeasurements: List<Measurement>
    ): Map<MeasurementType, List<Measurement>> {
        if (unsampledMeasurements.isEmpty()) return emptyMap()

        return unsampledMeasurements
            .asSequence()
            .filter { it.measurementTime >= startOfSampling }
            .sortedBy { it.measurementTime }
            .groupBy { it.type }
            .mapValues { (_, measurements) -> sampleMeasurements(measurements) }
    }

    private fun sampleMeasurements(
        sortedMeasurements: List<Measurement>
    ): List<Measurement> {
        if (sortedMeasurements.isEmpty()) return emptyList()

        val result = mutableListOf<Measurement>()
        var lastMeasurement = sortedMeasurements.first()
        var currentInterval = getNextIntervalTime(lastMeasurement.measurementTime)

        sortedMeasurements.forEach { measurement ->
            if (measurement.measurementTime <= currentInterval) {
                lastMeasurement = measurement // measurement belongs to the current interval
            } else { // measurement belongs to the next interval (time > current interval)
                result.add(createIntervalMeasurement(currentInterval, lastMeasurement))
                lastMeasurement = measurement
                currentInterval = getNextIntervalTime(measurement.measurementTime)
            }
        }
        result.add(createIntervalMeasurement(currentInterval, lastMeasurement))
        return result
    }

    private fun getNextIntervalTime(measurementTime: LocalDateTime): LocalDateTime {
        val minute = measurementTime.minute
        val currentInterval = minute / 5 * 5
        val nextInterval = currentInterval + 5

        return measurementTime
            .withSecond(0)
            .withNano(0)
            .withMinute(if (minute == currentInterval && measurementTime.second == 0 && measurementTime.nano == 0)
                currentInterval
            else nextInterval)
    }

    private fun createIntervalMeasurement(interval: LocalDateTime, measurement: Measurement) =
        measurement.copy(measurementTime = interval)
}