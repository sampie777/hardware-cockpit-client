package nl.sajansen.hardwarecockpitclient.hardware.serial

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortEvent
import nl.sajansen.hardwarecockpitclient.config.Config
import nl.sajansen.hardwarecockpitclient.connectors.ConnectorRegister
import nl.sajansen.hardwarecockpitclient.mocks.TestConnector
import nl.sajansen.hardwarecockpitclient.mocks.TestHardwareDevice
import kotlin.test.*


class SerialListenerTest {

    private val hardwareDevice = TestHardwareDevice()
    private val serialListener = SerialListener(hardwareDevice)
    private val serialPort = SerialPort.getCommPort("test-port")

    @BeforeTest
    fun before() {
        ConnectorRegister.unregisterAll()
        serialListener.clear()
    }

    @Test
    fun testValidateMetaForData() {
        val metaByte = Config.serialMetaBits.shl(0x05).toByte()
        val result = serialListener.validateMetaForData(arrayListOf(metaByte))
        assertTrue(result)
    }

    @Test
    fun testValidateMetaForDataWithInvalidMetaBit() {
        val metaByte = (Config.serialMetaBits - 1).shl(0x05).toByte()
        val result = serialListener.validateMetaForData(arrayListOf(metaByte))
        assertFalse(result)
    }

    @Test
    fun testGetUpdateTypeForData() {
        val metaByte = SerialDataType.TOGGLE.id.and(0x07).shl(0x02).toByte()
        val result = serialListener.getUpdateTypeForData(arrayListOf(metaByte))
        assertEquals(SerialDataType.TOGGLE, result)
    }

    @Test
    fun testGetUpdateTypeForDataForInvalidType() {
        val metaByte = 255.and(0x07).shl(0x02).toByte()
        val result = serialListener.getUpdateTypeForData(arrayListOf(metaByte))
        assertNull(result)
    }

    @Test
    fun testGetComponentForData() {
        val idByte = 1.toByte()
        val result = serialListener.getComponentForData(arrayListOf(0, idByte))!!

        assertEquals(1, result.id)
        assertEquals(hardwareDevice.components[0], result)
    }

    @Test
    fun testGetComponentForDataWithInvalidId() {
        val idByte = 99.toByte()
        val result = serialListener.getComponentForData(arrayListOf(0, idByte))
        assertNull(result)
    }

    @Test
    fun testGetValueForDataWithZeroLength() {
        val metaByte = 0.and(0x03).toByte()
        val result = serialListener.getValueForData(arrayListOf(metaByte, 0))
        assertEquals(1, result)
    }

    @Test
    fun testGetValueForDataWithLengthOne() {
        val metaByte = 1.and(0x03).toByte()
        val result = serialListener.getValueForData(arrayListOf(metaByte, 0, 0))
        assertEquals(0, result)
    }

    @Test
    fun testGetValueForDataWithLengthOne1() {
        val metaByte = 1.and(0x03).toByte()
        val result = serialListener.getValueForData(arrayListOf(metaByte, 0, 8))
        assertEquals(8, result)
    }

    @Test
    fun testGetValueForDataWithLengthTwo() {
        val metaByte = 2.and(0x03).toByte()
        val result = serialListener.getValueForData(arrayListOf(metaByte, 0, 0, 0))
        assertEquals(0, result)
    }

    @Test
    fun testGetValueForDataWithLengthTwo1() {
        val metaByte = 2.and(0x03).toByte()
        val result = serialListener.getValueForData(arrayListOf(metaByte, 0, 1, 8))
        assertEquals(264, result)
    }

    @Test
    fun testGetValueForDataWithLengthTwo2() {
        val metaByte = 2.and(0x03).toByte()
        val result = serialListener.getValueForData(arrayListOf(metaByte, 0, 1, 0xff.toByte()))
        assertEquals(511, result)
    }

    @Test
    fun testGetValueForDataWithLengthThree() {
        val metaByte = 3.and(0x03).toByte()
        val result = serialListener.getValueForData(arrayListOf(metaByte, 0, 0, 0, 8))
        assertEquals(8, result)
    }

    @Test
    fun testGetValueForDataWithIncorrectLength() {
        val metaByte = 3.and(0x03).toByte()
        val result = serialListener.getValueForData(arrayListOf(metaByte, 0, 8))
        assertNull(result)
    }

    @Test
    fun testGetValueForDataWithIncorrectLength1() {
        val metaByte = 1.and(0x03).toByte()
        val result = serialListener.getValueForData(arrayListOf(metaByte, 0, 8, 2, 3, 4))
        assertEquals(8, result)
    }

    @Test
    fun testButtonToggleCommandReachesConnector() {
        val data = byteArrayOf(createMetaByte(SerialDataType.TOGGLE, 0), 1)

        val testConnector = TestConnector()
        testConnector.enable()

        val event = SerialPortEvent(serialPort, SerialPort.LISTENING_EVENT_DATA_RECEIVED, data)

        // When
        serialListener.serialEvent(event)

        assertTrue(testConnector.valueUpdated)
        assertEquals(hardwareDevice.NAME_BUTTON_1, testConnector.valueUpdatedWithKey)
        assertEquals(true, testConnector.valueUpdatedWithValue) // True, because it comes from a button component
    }

    @Test
    fun testSliderValueCommandReachesConnector() {
        val data = byteArrayOf(createMetaByte(SerialDataType.ABSOLUTE_VALUE, 2), 3, 2, 3)

        val testConnector = TestConnector()
        testConnector.enable()

        val event = SerialPortEvent(serialPort, SerialPort.LISTENING_EVENT_DATA_RECEIVED, data)

        // When
        serialListener.serialEvent(event)

        assertTrue(testConnector.valueUpdated)
        assertEquals(hardwareDevice.NAME_SLIDER_3, testConnector.valueUpdatedWithKey)
        assertEquals(515, testConnector.valueUpdatedWithValue) // True, because it comes from a button component
    }

    @Test
    fun testSliderValueCommandReachesConnectorByteByByte() {
        val data = byteArrayOf(createMetaByte(SerialDataType.ABSOLUTE_VALUE, 2), 3, 2, 3)

        val testConnector = TestConnector()
        testConnector.enable()

        // When
        sendSerialEventWithData(byteArrayOf(data[0]))
        sendSerialEventWithData(byteArrayOf(data[1], data[2]))
        sendSerialEventWithData(byteArrayOf(data[3]))

        assertTrue(testConnector.valueUpdated)
        assertEquals(hardwareDevice.NAME_SLIDER_3, testConnector.valueUpdatedWithKey)
        assertEquals(515, testConnector.valueUpdatedWithValue) // True, because it comes from a button component
    }

    @Test
    fun testReceivedDataBufferGetsCleared() {
        val data = byteArrayOf(createMetaByte(SerialDataType.ABSOLUTE_VALUE, 2), 3, 2, 3)

        // When
        sendSerialEventWithData(byteArrayOf(data[0]))
        assertEquals(1, serialListener.receivedData.size)
        sendSerialEventWithData(byteArrayOf(data[1], data[2]))
        assertEquals(3, serialListener.receivedData.size)
        sendSerialEventWithData(byteArrayOf(data[3]))
        println(serialListener.receivedData)
        assertEquals(0, serialListener.receivedData.size)
    }

    @Test
    fun testDataSurroundedWithIncorrectDataGetsProcessed() {
        val data = byteArrayOf(createMetaByte(SerialDataType.ABSOLUTE_VALUE, 2), 3, 2, 3)

        val testConnector = TestConnector()
        testConnector.enable()

        // When
        sendSerialEventWithData(byteArrayOf(99))
        sendSerialEventWithData(byteArrayOf(99, data[0]))
        sendSerialEventWithData(byteArrayOf(data[1], data[2]))
        sendSerialEventWithData(byteArrayOf(data[3], 99))
        sendSerialEventWithData(byteArrayOf(99))

        assertTrue(testConnector.valueUpdated)
        assertEquals(hardwareDevice.NAME_SLIDER_3, testConnector.valueUpdatedWithKey)
        assertEquals(515, testConnector.valueUpdatedWithValue) // True, because it comes from a button component
    }

    @Test
    fun testIncorrectDataGetsProcessedUntilValidData() {
        val data = byteArrayOf(createMetaByte(SerialDataType.ABSOLUTE_VALUE, 2), 3, 2, 3)

        val testConnector = TestConnector()
        testConnector.enable()
        // When
        sendSerialEventWithData(byteArrayOf(99))
        assertEquals(1, serialListener.receivedData.size)
        sendSerialEventWithData(byteArrayOf(0))
        assertEquals(1, serialListener.receivedData.size)
        sendSerialEventWithData(byteArrayOf(data[0]))
        assertEquals(1, serialListener.receivedData.size)
        sendSerialEventWithData(byteArrayOf(data[1]))
        assertEquals(2, serialListener.receivedData.size)
        sendSerialEventWithData(byteArrayOf(data[2]))
        assertEquals(3, serialListener.receivedData.size)
        sendSerialEventWithData(byteArrayOf(data[3]))
        assertEquals(0, serialListener.receivedData.size)

        assertTrue(testConnector.valueUpdated)
        assertEquals(hardwareDevice.NAME_SLIDER_3, testConnector.valueUpdatedWithKey)
        assertEquals(515, testConnector.valueUpdatedWithValue) // True, because it comes from a button component
    }

    @Test
    fun testDataValueSimilarToMetaByteWillBeTreatedAsValue() {
        val data = byteArrayOf(
            createMetaByte(SerialDataType.ABSOLUTE_VALUE, 2),
            3,
            createMetaByte(SerialDataType.RELATIVE_VALUE, 2),
            3
        )

        val testConnector = TestConnector()
        testConnector.enable()

        // When
        sendSerialEventWithData(byteArrayOf(data[0]))
        assertEquals(1, serialListener.receivedData.size)
        sendSerialEventWithData(byteArrayOf(data[1]))
        assertEquals(2, serialListener.receivedData.size)
        sendSerialEventWithData(byteArrayOf(data[2]))
        assertEquals(3, serialListener.receivedData.size)
        sendSerialEventWithData(byteArrayOf(data[3]))
        assertEquals(0, serialListener.receivedData.size)

        assertTrue(testConnector.valueUpdated)
        assertEquals(hardwareDevice.NAME_SLIDER_3, testConnector.valueUpdatedWithKey)
        assertEquals(515, testConnector.valueUpdatedWithValue) // True, because it comes from a button component
    }

    private fun sendSerialEventWithData(data: ByteArray) {
        serialListener.serialEvent(
            SerialPortEvent(
                serialPort,
                SerialPort.LISTENING_EVENT_DATA_RECEIVED,
                data
            )
        )
    }

    private fun createMetaByte(updateType: SerialDataType, dataLength: Int): Byte {
        return (Config.serialMetaBits.shl(0x05)
                + updateType.id.and(0x07).shl(0x02)
                + dataLength.and(0x03)).toByte()
    }
}