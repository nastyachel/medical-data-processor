package sampler

import java.time.LocalDateTime
import java.util.*

/**
 * Samples measurements into fixed intervals.
 * Each interval uses the last measurement within that timeframe.
 *
 * @property intervalMinutes Sampling interval in minutes, defaults to 5. Must evenly divide 60 minutes to intervals.
 * @throws IllegalArgumentException if intervalMinutes does not evenly divide 60
 */
class MeasurementSampler(private val intervalMinutes: Int = 5) {
    init {
        require(60 % intervalMinutes == 0) {
            "Interval must evenly divide 60 minutes. " +
                    "Valid values: 1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60"
        }
    }

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
            .groupBy { it.type }
            .mapValues { (_, measurements) -> sampleMeasurements(measurements) }
    }

    private fun sampleMeasurements(measurements: List<Measurement>): List<Measurement> {
        val measurementsByInterval: MutableMap<LocalDateTime, TreeSet<Measurement>> = TreeMap()

        measurements.forEach { measurement ->
            val interval = getNextIntervalTime(measurement.measurementTime)
            if (!measurementsByInterval.containsKey(interval)) {
                measurementsByInterval[interval] = TreeSet(compareBy { it.measurementTime })
            }
            measurementsByInterval[interval]?.add(measurement)
        }
        val result = measurementsByInterval.mapNotNull { (interval, measurementsInInterval) ->
            createIntervalMeasurement(interval, measurementsInInterval.last())
        }.toList()
        return result
    }

    private fun getNextIntervalTime(measurementTime: LocalDateTime): LocalDateTime {
        val intervalLowerBound =
            measurementTime.minute / intervalMinutes * intervalMinutes // rounding to the lower bound of interval
        val intervalUpperBound = intervalLowerBound + intervalMinutes
        return when {
            isExactlyOnInterval(measurementTime, intervalLowerBound) -> measurementTime
            isLastIntervalOfHour(intervalLowerBound) -> measurementTime.plusHours(1).startOfHour()
            else -> measurementTime.withMinute(intervalUpperBound).startOfMinute()
        }
    }

    private fun isExactlyOnInterval(time: LocalDateTime, interval: Int) =
        time.minute == interval && time.second == 0

    private fun isLastIntervalOfHour(interval: Int) =
        interval + intervalMinutes == 60

    private fun LocalDateTime.startOfHour() =
        withMinute(0).startOfMinute()

    private fun LocalDateTime.startOfMinute() =
        withSecond(0)

    private fun createIntervalMeasurement(interval: LocalDateTime, measurement: Measurement) =
        measurement.copy(measurementTime = interval)
}