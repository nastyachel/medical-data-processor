package sampler
import java.time.Duration
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
            .groupBy { it.type }
            .mapValues { (_, measurements) ->
                sampleMeasurements(startOfSampling, measurements.sortedBy { it.measurementTime })
            }
    }

    private fun sampleMeasurements(
        startOfSampling: LocalDateTime,
        sortedMeasurements: List<Measurement>
    ): List<Measurement> {
        if (sortedMeasurements.isEmpty()) return emptyList()

        val result = mutableListOf<Measurement>()
        var currentMeasurement = sortedMeasurements.first()
        var currentInterval = getNextIntervalTime(startOfSampling, currentMeasurement.measurementTime)

        sortedMeasurements.forEach { measurement ->
            if (measurement.measurementTime <= currentInterval) {
                currentMeasurement = measurement
            } else {
                result.add(createIntervalMeasurement(currentInterval, currentMeasurement))
                currentMeasurement = measurement
                currentInterval = getNextIntervalTime(currentInterval, measurement.measurementTime)
            }
        }
        result.add(createIntervalMeasurement(currentInterval, currentMeasurement))
        return result
    }

    private fun getNextIntervalTime(startTime: LocalDateTime, measurementTime: LocalDateTime): LocalDateTime {
        val durationInSeconds = Duration.between(startTime, measurementTime).seconds
        val intervalInSeconds = 5 * 60
        val intervalCount = (durationInSeconds + intervalInSeconds - 1) / intervalInSeconds

        return startTime.plusSeconds(intervalCount * intervalInSeconds)
    }

    private fun createIntervalMeasurement(interval: LocalDateTime, measurement: Measurement) =
        measurement.copy(measurementTime = interval)
}