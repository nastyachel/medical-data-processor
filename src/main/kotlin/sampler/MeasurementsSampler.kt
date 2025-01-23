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
                lastMeasurement = measurement // measurement belongs to the current interval, not added to the result yet
            } else {
                // measurement belongs to the next interval (time > current interval)
                // it means that previous measurement was the last one for previous interval,
                // previous measurement should be added to result
                result.add(createIntervalMeasurement(currentInterval, lastMeasurement))
                lastMeasurement = measurement
                currentInterval = getNextIntervalTime(measurement.measurementTime)
            }
        }
        result.add(createIntervalMeasurement(currentInterval, lastMeasurement))
        return result
    }

    private fun getNextIntervalTime(measurementTime: LocalDateTime): LocalDateTime {
        val intervalLowerBound = measurementTime.minute / intervalMinutes * intervalMinutes // rounding to the lower bound of interval
        val intervalUpperBound = intervalLowerBound + intervalMinutes
        return when {
            isExactlyOnInterval(measurementTime, intervalLowerBound) -> measurementTime
            isLastIntervalOfHour(intervalLowerBound) -> measurementTime.plusHours(1).startOfHour()
            else -> measurementTime.withMinute(intervalUpperBound).startOfMinute()
        }
    }

    private fun isExactlyOnInterval(time: LocalDateTime, interval: Int) =
        time.minute == interval && time.second == 0 && time.nano == 0

    private fun isLastIntervalOfHour(interval: Int) =
        interval + intervalMinutes == 60

    private fun LocalDateTime.startOfHour() =
        withMinute(0).startOfMinute()

    private fun LocalDateTime.startOfMinute() =
        withSecond(0).withNano(0)

    private fun createIntervalMeasurement(interval: LocalDateTime, measurement: Measurement) =
        measurement.copy(measurementTime = interval)
}