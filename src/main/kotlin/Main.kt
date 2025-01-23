package org.example

import sampler.Measurement
import sampler.MeasurementSampler
import sampler.MeasurementType
import java.time.LocalDateTime


fun main() {
    val measurements = prepareTestMeasurements()

    val startTime = LocalDateTime.parse("2017-01-03T10:00:00")
    val sampler = MeasurementSampler()

    val formattedResults = formatResults(sampler.sample(startTime, measurements))
    formattedResults.forEach(::println)
}

private fun prepareTestMeasurements() = listOf(
    measurement("2017-01-03T10:04:45", 35.79, MeasurementType.TEMP),
    measurement("2017-01-03T10:01:18", 98.78, MeasurementType.SPO2),
    measurement("2017-01-03T10:09:07", 35.01, MeasurementType.TEMP),
    measurement("2017-01-03T10:03:34", 96.49, MeasurementType.SPO2),
    measurement("2017-01-03T10:02:01", 35.82, MeasurementType.TEMP),
    measurement("2017-01-03T10:05:00", 97.17, MeasurementType.SPO2),
    measurement("2017-01-03T10:05:01", 95.08, MeasurementType.SPO2)
)

private fun measurement(time: String, value: Double, type: MeasurementType) =
    Measurement(LocalDateTime.parse(time), value, type)

private fun formatResults(results: Map<MeasurementType, List<Measurement>>) = results.entries
    .flatMap { (type, measurements) ->
        measurements.map { m -> formatMeasurement(m, type) }
    }
    .sorted()

private fun formatMeasurement(m: Measurement, type: MeasurementType) =
    "{${m.measurementTime}, $type, %.2f}".format(m.measurementValue)