import sampler.Measurement
import sampler.MeasurementSampler
import sampler.MeasurementType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

class MeasurementSamplerTest {
    private val sampler = MeasurementSampler()

    @Test
    fun `test sampling performed on given data`() {
        val measurements = listOf(
            Measurement(LocalDateTime.parse("2017-01-03T10:04:45"), 35.79, MeasurementType.TEMP),
            Measurement(LocalDateTime.parse("2017-01-03T10:01:18"), 98.78, MeasurementType.SPO2),
            Measurement(LocalDateTime.parse("2017-01-03T10:09:07"), 35.01, MeasurementType.TEMP),
            Measurement(LocalDateTime.parse("2017-01-03T10:03:34"), 96.49, MeasurementType.SPO2),
            Measurement(LocalDateTime.parse("2017-01-03T10:02:01"), 35.82, MeasurementType.TEMP),
            Measurement(LocalDateTime.parse("2017-01-03T10:05:00"), 97.17, MeasurementType.SPO2),
            Measurement(LocalDateTime.parse("2017-01-03T10:05:01"), 95.08, MeasurementType.SPO2)
        )

        val startTime = LocalDateTime.parse("2017-01-03T10:05:00")
        val result = sampler.sample(startTime, measurements)

        val expectedTemp = listOf(
            Measurement(LocalDateTime.parse("2017-01-03T10:05:00"), 35.79, MeasurementType.TEMP),
            Measurement(LocalDateTime.parse("2017-01-03T10:10:00"), 35.01, MeasurementType.TEMP)
        )
        val expectedSpo2 = listOf(
            Measurement(LocalDateTime.parse("2017-01-03T10:05:00"), 97.17, MeasurementType.SPO2),
            Measurement(LocalDateTime.parse("2017-01-03T10:10:00"), 95.08, MeasurementType.SPO2)
        )

        assertEquals(expectedTemp, result[MeasurementType.TEMP])
        assertEquals(expectedSpo2, result[MeasurementType.SPO2])
    }

    @Test
    fun `test empty input`() {
        val startTime = LocalDateTime.parse("2017-01-03T10:00:00")
        val result = sampler.sample(startTime, emptyList())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `test on interval matching measurements time`() {
        val measurements = listOf(
            Measurement(LocalDateTime.parse("2017-01-03T10:05:00"), 36.0, MeasurementType.TEMP),
            Measurement(LocalDateTime.parse("2017-01-03T10:10:00"), 36.5, MeasurementType.TEMP)
        )

        val startTime = LocalDateTime.parse("2017-01-03T10:00:00")
        val result = sampler.sample(startTime, measurements)

        val expected = listOf(
            Measurement(LocalDateTime.parse("2017-01-03T10:05:00"), 36.0, MeasurementType.TEMP),
            Measurement(LocalDateTime.parse("2017-01-03T10:10:00"), 36.5, MeasurementType.TEMP)
        )
        assertEquals(expected, result[MeasurementType.TEMP])
    }

    @Test
    fun `test single measurement type`() {
        val measurements = listOf(
            Measurement(LocalDateTime.parse("2017-01-03T10:02:00"), 36.0, MeasurementType.TEMP),
            Measurement(LocalDateTime.parse("2017-01-03T10:04:00"), 36.5, MeasurementType.TEMP)
        )

        val startTime = LocalDateTime.parse("2017-01-03T10:00:00")
        val result = sampler.sample(startTime, measurements)

        val expected = listOf(
            Measurement(LocalDateTime.parse("2017-01-03T10:05:00"), 36.5, MeasurementType.TEMP)
        )
        assertEquals(expected, result[MeasurementType.TEMP])
    }


    @Test
    fun `test multiple measurements matches interval`() {
        val measurements = listOf(
            Measurement(LocalDateTime.parse("2017-01-03T10:05:00"), 36.0, MeasurementType.TEMP),
            Measurement(LocalDateTime.parse("2017-01-03T10:05:00"), 36.5, MeasurementType.TEMP)
        )
        val result = sampler.sample(LocalDateTime.parse("2017-01-03T10:00:00"), measurements)
        assertEquals(1, result[MeasurementType.TEMP]?.size)
        assertTrue(result[MeasurementType.TEMP]?.first()?.measurementValue in setOf(36.0, 36.5))
    }

    @Test
    fun `test measurements spanning multiple intervals`() {
        val measurements = listOf(
            Measurement(LocalDateTime.parse("2017-01-03T10:01:00"), 36.0, MeasurementType.TEMP),
            Measurement(LocalDateTime.parse("2017-01-03T10:14:00"), 36.5, MeasurementType.TEMP)
        )
        val result = sampler.sample(LocalDateTime.parse("2017-01-03T10:00:00"), measurements)

        val expectedTemp = listOf(
            Measurement(LocalDateTime.parse("2017-01-03T10:05:00"), 36.0, MeasurementType.TEMP),
            Measurement(LocalDateTime.parse("2017-01-03T10:15:00"), 36.5, MeasurementType.TEMP)
        )
        assertEquals(expectedTemp, result[MeasurementType.TEMP])
    }

    @Test
    fun `test on start time after first measurement`() {
        val measurements = listOf(
            Measurement(LocalDateTime.parse("2017-01-03T10:01:00"), 36.0, MeasurementType.TEMP)
        )
        val expectedTemp = listOf(
            Measurement(LocalDateTime.parse("2017-01-03T10:02:00"), 36.0, MeasurementType.TEMP)
        )
        val result = sampler.sample(LocalDateTime.parse("2017-01-03T10:02:00"), measurements)
        assertEquals(expectedTemp, result[MeasurementType.TEMP])
    }

    @Test
    fun `test on start time before first measurement`() {
        val measurements = listOf(
            Measurement(LocalDateTime.parse("2017-01-03T10:01:00"), 36.0, MeasurementType.TEMP)
        )
        val expectedTemp = listOf(
            Measurement(LocalDateTime.parse("2017-01-03T10:05:02"), 36.0, MeasurementType.TEMP)
        )
        val result = sampler.sample(LocalDateTime.parse("2017-01-03T10:00:02"), measurements)
        assertEquals(expectedTemp, result[MeasurementType.TEMP])
    }

}