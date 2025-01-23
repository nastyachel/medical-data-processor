import sampler.Measurement
import sampler.MeasurementSampler
import sampler.MeasurementType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

class MeasurementSamplerTest {
    private val sampler = MeasurementSampler()
    private val defaultStartTime = LocalDateTime.parse("2017-01-03T10:00:00")

    private fun measurement(time: String, value: Double, type: MeasurementType = MeasurementType.TEMP) =
        Measurement(LocalDateTime.parse(time), value, type)

    @Test
    fun `test sampling performed on given data`() {
        val measurements = listOf(
            measurement("2017-01-03T10:04:45", 35.79, MeasurementType.TEMP),
            measurement("2017-01-03T10:01:18", 98.78, MeasurementType.SPO2),
            measurement("2017-01-03T10:09:07", 35.01, MeasurementType.TEMP),
            measurement("2017-01-03T10:03:34", 96.49, MeasurementType.SPO2),
            measurement("2017-01-03T10:02:01", 35.82, MeasurementType.TEMP),
            measurement("2017-01-03T10:05:00", 97.17, MeasurementType.SPO2),
            measurement("2017-01-03T10:05:01", 95.08, MeasurementType.SPO2)
        )
        val expectedTemp = listOf(
            measurement("2017-01-03T10:05:00", 35.79, MeasurementType.TEMP),
            measurement("2017-01-03T10:10:00", 35.01, MeasurementType.TEMP)
        )
        val expectedSpo2 = listOf(
            measurement("2017-01-03T10:05:00", 97.17, MeasurementType.SPO2),
            measurement("2017-01-03T10:10:00", 95.08, MeasurementType.SPO2)
        )

        val result = sampler.sample(defaultStartTime, measurements)
        assertEquals(expectedTemp, result[MeasurementType.TEMP])
        assertEquals(expectedSpo2, result[MeasurementType.SPO2])
    }

    @Test
    fun `test empty input`() {
        val result = sampler.sample(defaultStartTime, emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test on interval matching measurements time`() {
        val measurements = listOf(
            measurement("2017-01-03T10:05:00", 36.0),
            measurement("2017-01-03T10:10:00", 36.5)
        )
        val expected = listOf(
            measurement("2017-01-03T10:05:00", 36.0),
            measurement("2017-01-03T10:10:00", 36.5)
        )

        val result = sampler.sample(defaultStartTime, measurements)
        assertEquals(expected, result[MeasurementType.TEMP])
    }

    @Test
    fun `test single measurement type`() {
        val measurements = listOf(
            measurement("2017-01-03T10:02:00", 36.0),
            measurement("2017-01-03T10:04:00", 36.5)
        )
        val expected = listOf(
            measurement("2017-01-03T10:05:00", 36.5)
        )

        val result = sampler.sample(defaultStartTime, measurements)
        assertEquals(expected, result[MeasurementType.TEMP])
    }


    @Test
    fun `test multiple measurements matches interval`() {
        val measurements = listOf(
            measurement("2017-01-03T10:05:00", 36.0),
            measurement("2017-01-03T10:05:00", 36.5)
        )

        val result = sampler.sample(defaultStartTime, measurements)
        assertEquals(1, result[MeasurementType.TEMP]?.size)
        assertTrue(result[MeasurementType.TEMP]?.first()?.measurementValue in setOf(36.0, 36.5))
    }

    @Test
    fun `test measurements gaps that skip multiple intervals`() {
        val measurements = listOf(
            measurement("2017-01-03T10:01:00", 36.0),
            measurement("2017-01-03T10:14:00", 36.5)
        )
        val expectedTemp = listOf(
            measurement("2017-01-03T10:05:00", 36.0),
            measurement("2017-01-03T10:15:00", 36.5)
        )

        val result = sampler.sample(defaultStartTime, measurements)
        assertEquals(expectedTemp, result[MeasurementType.TEMP])
    }

    @Test
    fun `test on start time after first measurement (filtering out older measurements)`() {
        val measurements = listOf(
            measurement("2017-01-03T10:01:00", 36.1),
            measurement("2017-01-03T10:04:00", 36.0)
        )
        val expectedTemp = listOf(
            measurement("2017-01-03T10:05:00", 36.0)
        )
        val startTime = LocalDateTime.parse("2017-01-03T10:02:00")

        val result = sampler.sample(startTime, measurements)
        assertEquals(expectedTemp, result[MeasurementType.TEMP])
    }

    @Test
    fun `test on start time before first measurement (no filtering)`() {
        val measurements = listOf(
            measurement("2017-01-03T10:01:00", 36.0)
        )
        val expectedTemp = listOf(
            measurement("2017-01-03T10:05:00", 36.0)
        )
        val startTime = LocalDateTime.parse("2017-01-03T10:00:02")

        val result = sampler.sample(startTime, measurements)
        assertEquals(expectedTemp, result[MeasurementType.TEMP])
    }

    @Test
    fun `test on switching to next day (edge case)`() {
        val measurements = listOf(
            measurement("2017-01-03T23:57:00", 36.0)
        )
        val expectedTemp = listOf(
            measurement("2017-01-04T00:00:00", 36.0)
        )
        val startTime = LocalDateTime.parse("2017-01-03T10:00:02")

        val result = sampler.sample(startTime, measurements)
        assertEquals(expectedTemp, result[MeasurementType.TEMP])
    }

    @Test
    fun `test on measurement taken on midnight`() {
        val measurements = listOf(
            measurement("2017-01-03T00:00:00", 36.0)
        )
        val expectedTemp = listOf(
            measurement("2017-01-03T00:00:00", 36.0)
        )
        val startTime = LocalDateTime.parse("2017-01-02T10:00:02")

        val result = sampler.sample(startTime, measurements)
        assertEquals(expectedTemp, result[MeasurementType.TEMP])
    }

    @Test
    fun `test on start of the new day`() {
        val measurements = listOf(
            measurement("2017-01-03T00:01:00", 36.0)
        )
        val expectedTemp = listOf(
            measurement("2017-01-03T00:05:00", 36.0)
        )
        val startTime = LocalDateTime.parse("2017-01-02T10:00:02")

        val result = sampler.sample(startTime, measurements)
        assertEquals(expectedTemp, result[MeasurementType.TEMP])
    }

    @Test
    fun `test on 30 minute interval`() {
        val newSampler = MeasurementSampler(30)

        val measurements = listOf(
            measurement("2017-01-03T10:35:00", 36.0)
        )
        val expectedTemp = listOf(
            measurement("2017-01-03T11:00:00", 36.0)
        )
        val startTime = LocalDateTime.parse("2017-01-02T10:00:02")

        val result = newSampler.sample(startTime, measurements)
        assertEquals(expectedTemp, result[MeasurementType.TEMP])
    }

}