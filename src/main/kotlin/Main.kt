package org.example

import sampler.Measurement
import sampler.MeasurementSampler
import sampler.MeasurementType
import java.time.LocalDateTime


fun main() {

    val measurements = listOf(
        Measurement(
            LocalDateTime.parse("2017-01-03T10:04:45"), 35.79, MeasurementType.TEMP
        ),
        Measurement(
            LocalDateTime.parse("2017-01-03T10:01:18"), 98.78, MeasurementType.SPO2
        ),
        Measurement(
            LocalDateTime.parse("2017-01-03T10:09:07"), 35.01, MeasurementType.TEMP
        ),
        Measurement(
            LocalDateTime.parse("2017-01-03T10:03:34"), 96.49, MeasurementType.SPO2
        ),
        Measurement(
            LocalDateTime.parse("2017-01-03T10:02:01"), 35.82, MeasurementType.TEMP
        ),
        Measurement(
            LocalDateTime.parse("2017-01-03T10:05:00"), 97.17, MeasurementType.SPO2
        ),
        Measurement(
            LocalDateTime.parse("2017-01-03T10:05:01"), 95.08, MeasurementType.SPO2
        )
    )

    val startTime = LocalDateTime.parse("2017-01-03T10:05:00")

    val sampler = MeasurementSampler()
    val result = sampler.sample(startTime, measurements)

    println("OUTPUT:")
    result.entries
        .flatMap { (type, measurements) ->
            measurements.map { m ->
                String.format(
                    "{%s, %s, %.2f}",
                    m.measurementTime,
                    type,
                    m.measurementValue
                )
            }
        }
        .sorted()
        .forEach(::println)
}