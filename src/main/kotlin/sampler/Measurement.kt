package sampler

import java.time.LocalDateTime

/**
 * Represents a single medical measurement
 *
 * @property measurementTime When the measurement was taken
 * @property measurementValue Value of the measurement
 * @property type Type of measurement (temperature, SPO2, etc.)
 */
data class Measurement(
    val measurementTime: LocalDateTime,
    val measurementValue: Double,
    val type: MeasurementType
)