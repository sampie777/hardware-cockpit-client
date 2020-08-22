package nl.sajansen.hardwarecockpitclient.utils

import kotlin.test.Test
import kotlin.test.assertEquals


class NumberMapTest {

    @Test
    fun testLinearMapping() {
        val numberMap = NumberMap(0, 100, 0, 10, mode = NumberMapMode.LINEAR)
        assertEquals(0, numberMap.map(0))
        assertEquals(10, numberMap.map(1))
        assertEquals(100, numberMap.map(10))
    }

    @Test
    fun testAbsoluteMapping() {
        val numberMap = NumberMap(0, 10, mode = NumberMapMode.ABSOLUTE)
        assertEquals(0, numberMap.map(-10))
        assertEquals(0, numberMap.map(0))
        assertEquals(1, numberMap.map(1))
        assertEquals(10, numberMap.map(10))
        assertEquals(10, numberMap.map(20))
    }
}