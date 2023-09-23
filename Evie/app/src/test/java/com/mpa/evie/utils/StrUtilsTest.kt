package com.mpa.evie.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StrUtilsTest {

    companion object {
        private const val EXPECTED_RESULT = 10.toDouble()
        private const val EXPECTED_LARGE_RESULT = 1000.toDouble()
        private const val EXPECTED_BOUNDARY_RESULT = 100000000.toDouble()
    }

    @Test
    fun `test getCostPerKwhFromString with valid input`() {
        val result = getCostPerKwhFromString("£10/kWh")
        assertEquals(EXPECTED_RESULT, result)
    }

    @Test
    fun `test getCostPerKwhFromString with valid (decimal) input`() {
        val result = getCostPerKwhFromString("£10.00/kWh")
        assertEquals(EXPECTED_RESULT, result)
    }

    @Test
    fun `test getCostPerKwhFromString with null input`() {
        val result = getCostPerKwhFromString(null)
        assertNull(result)
    }

    @Test
    fun `test getCostPerKwhFromString with large input`() {
        val result = getCostPerKwhFromString("£1,000.00/kWh")
        assertEquals(EXPECTED_LARGE_RESULT, result)
    }

    @Test
    fun `test getCostPerKwhFromString with valid input no pound sign`() {
        val result = getCostPerKwhFromString("10.00/kWh")
        assertEquals(EXPECTED_RESULT, result)
    }

    @Test
    fun `test getCostPerKwhFromString with invalid input no kWh`() {
        val result = getCostPerKwhFromString("£1.00/h")
        assertNull(result)
    }

    @Test
    fun `test getCostPerKwhFromString with letter input`() {
        val result = getCostPerKwhFromString("£abcd/kWh")
        assertNull(result)
    }

    @Test
    fun `test getCostPerKwhFromString with boundary input`() {
        val result = getCostPerKwhFromString("£100,000,000/kWh")
        assertEquals(EXPECTED_BOUNDARY_RESULT, result)
    }
}