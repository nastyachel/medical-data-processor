package sampler

import java.time.LocalDateTime

/**
 * Samples measurements into fixed intervals.
 * Each interval uses the last measurement within that timeframe.
 *
 * @property intervalMinutes Sampling interval in minutes, defaults to 5
 */
class MeasurementSampler(private val intervalMinutes: Int = 5) {
    /**
     * Samples measurements by grouping them into fixed time intervals.
     * Selects the last measurement in each interval as the representative value.
     *
     * @param startOfSampling Only measurements after this time are included
     * @param unsampledMeasurements Measurements to be sampled
     * @return Map of measurement types to their sampled measurements
     */
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
            } else {
                // measurement belongs to the next interval (time > current interval)
                // => previous measurement was the last one for previous interval => should be added to result
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
        val currentInterval = minute / intervalMinutes * intervalMinutes
        val nextInterval = currentInterval + intervalMinutes

        return measurementTime
            .withSecond(0)
            .withNano(0)
            .withMinute(
                if (minute == currentInterval && measurementTime.second == 0 && measurementTime.nano == 0)
                    currentInterval
                else nextInterval
            )
    }

    private fun createIntervalMeasurement(interval: LocalDateTime, measurement: Measurement) =
        measurement.copy(measurementTime = interval)
}