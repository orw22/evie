package com.mpa.evie.utils

import com.google.android.gms.maps.model.LatLng
import junit.framework.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MapUtilsTest {

    companion object {
        private const val EXPECTED_DISTANCE = 93.20603447912947
        private const val DISTANCE_ZERO = 0.0
    }

    @Test
    fun `test haversine distance with normal inputs`() {
        val result = haversineDistance(LatLng(52.1, 1.1), LatLng(50.9, 2.1))
        assertEquals(EXPECTED_DISTANCE, result)
    }

    @Test
    fun `test haversine distance with identical latlng coordinates`() {
        val result = haversineDistance(LatLng(0.0, 0.0), LatLng(0.0, 0.0))
        assertEquals(DISTANCE_ZERO, result)
    }

    @Test
    fun `test haversine distance with boundary inputs`() {
        val result = haversineDistance(LatLng(89.9, 179.2), LatLng(88.6, 180.0))
        assertNotEquals(DISTANCE_ZERO, result)
        assertNotNull(result)
    }

}