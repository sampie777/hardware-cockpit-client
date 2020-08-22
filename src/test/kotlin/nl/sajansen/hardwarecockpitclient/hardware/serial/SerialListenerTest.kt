package nl.sajansen.hardwarecockpitclient.hardware.serial

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortEvent
import nl.sajansen.hardwarecockpitclient.config.Config
import nl.sajansen.hardwarecockpitclient.connectors.ConnectorRegister
import nl.sajansen.hardwarecockpitclient.mocks.ConnectorMock
import nl.sajansen.hardwarecockpitclient.mocks.HardwareDeviceMock
import kotlin.test.*


class SerialListenerTest {

    private val hardwareDevice = HardwareDeviceMock()
    private val serialListener = SerialListener(hardwareDevice)
    private val serialPort = SerialPort.getCommPort("test-port")

    @BeforeTest
    fun before() {
        ConnectorRegister.unregisterAll()
        serialListener.clear()
    }

    @Test
    fun testValidateMetaForData() {
        val metaByte = Config.serialMetaBitsValue.shl(0x05).toByte()
        val result = serialListener.validateMetaForData(arrayListOf(metaByte))
        assertTrue(result)
    }

    @Test
    fun testValidateMetaForDataWithInvalidMetaBit() {
        val metaByte = (Config.serialMetaBitsValue - 1).shl(0x05).toByte()
        val result = serialListener.validateMetaForData(arrayListOf(metaByte))
        assertFalse(result)
    }

    @Test
    fun testGetUpdateTypeForData() {
        serialListener.metaByte = SerialDataType.TOGGLE.id.and(0x07).shl(0x02)
        val result = serialListener.getUpdateTypeForData()
        assertEquals(SerialDataType.TOGGLE, result)
    }

    @Test
    fun testGetUpdateTypeForDataForInvalidType() {
        serialListener.metaByte = 255.and(0x07).shl(0x02)
        val result = serialListener.getUpdateTypeForData()
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
    fun testGetComponentForDataUnsignedTest() {
        val idByte = 240.toByte()
        val result = serialListener.getComponentForData(arrayListOf(0, idByte))

        assertNotNull(result)
        assertEquals(240, result.id)
        assertEquals(hardwareDevice.components[4], result)
    }

    @Test
    fun testGetDataLengthForMetaByte() {
        var metaByte = 1.and(0x03)
        var result = serialListener.getDataLengthForMetaByte(metaByte)
        assertEquals(1, result)

        metaByte = 2.and(0x03)
        result = serialListener.getDataLengthForMetaByte(metaByte)
        assertEquals(2, result)
    }

    @Test
    fun testGetValueForDataWithZeroLength() {
        serialListener.dataLength = 0
        val result = serialListener.getValueForData(arrayListOf(0, 0))
        assertEquals(1, result)
    }

    @Test
    fun testGetValueForDataWithLengthOne() {
        serialListener.dataLength = 1
        val result = serialListener.getValueForData(arrayListOf(0, 0, 0))
        assertEquals(0, result)
    }

    @Test
    fun testGetValueForDataWithLengthOne1() {
        serialListener.dataLength = 1
        val result = serialListener.getValueForData(arrayListOf(0, 0, 8))
        assertEquals(8, result)
    }

    @Test
    fun testGetValueForDataWithLengthTwo() {
        serialListener.dataLength = 2
        val result = serialListener.getValueForData(arrayListOf(0, 0, 0, 0))
        assertEquals(0, result)
    }

    @Test
    fun testGetValueForDataWithLengthTwo1() {
        serialListener.dataLength = 2
        val result = serialListener.getValueForData(arrayListOf(0, 0, 1, 8))
        assertEquals(264, result)
    }

    @Test
    fun testGetValueForDataWithLengthTwo2() {
        serialListener.dataLength = 2
        val result = serialListener.getValueForData(arrayListOf(0, 0, 1, 0xff.toByte()))
        assertEquals(511, result)
    }

    @Test
    fun testGetValueForDataWithLengthThree() {
        serialListener.dataLength = 3
        val result = serialListener.getValueForData(arrayListOf(0, 0, 0, 0, 8))
        assertEquals(8, result)
    }

    @Test
    fun testGetValueForDataWithIncorrectLength() {
        serialListener.dataLength = 3
        val result = serialListener.getValueForData(arrayListOf(0, 0, 8))
        assertNull(result)
    }

    @Test
    fun testGetValueForDataWithIncorrectLength1() {
        serialListener.dataLength = 1
        val result = serialListener.getValueForData(arrayListOf(0, 0, 8, 2, 3, 4))
        assertEquals(8, result)
    }

    @Test
    fun testButtonToggleCommandReachesConnector() {
        val data = byteArrayOf(createMetaByte(SerialDataType.TOGGLE, 0), 1)

        val testConnector = ConnectorMock()
        testConnector.enable()

        val event = SerialPortEvent(serialPort, SerialPort.LISTENING_EVENT_DATA_RECEIVED, data)

        // When
        serialListener.serialEvent(event)

        assertTrue(testConnector.valueUpdated)
        assertEquals(hardwareDevice.NAME_BUTTON_1, testConnector.valueUpdatedWithKey)
        assertEquals(true, testConnector.valueUpdatedWithValue) // True, because it comes from a button component
    }

    @Test
    fun testSwitchToggleCommandReachesConnector() {
        val testConnector = ConnectorMock()
        testConnector.enable()

        val dataOn = byteArrayOf(createMetaByte(SerialDataType.BOOLEAN, 1), 2, 1)
        val eventOn = SerialPortEvent(serialPort, SerialPort.LISTENING_EVENT_DATA_RECEIVED, dataOn)

        // When
        serialListener.serialEvent(eventOn)

        assertTrue(testConnector.valueUpdated)
        assertEquals(hardwareDevice.NAME_SWITCH_2, testConnector.valueUpdatedWithKey)
        assertEquals(true, testConnector.valueUpdatedWithValue)

        val dataOff = byteArrayOf(createMetaByte(SerialDataType.BOOLEAN, 1), 2, 0)
        val eventOff = SerialPortEvent(serialPort, SerialPort.LISTENING_EVENT_DATA_RECEIVED, dataOff)

        // When
        serialListener.serialEvent(eventOff)

        assertTrue(testConnector.valueUpdated)
        assertEquals(hardwareDevice.NAME_SWITCH_2, testConnector.valueUpdatedWithKey)
        assertEquals(false, testConnector.valueUpdatedWithValue)
    }

    @Test
    fun testSliderValueCommandReachesConnector() {
        val data = byteArrayOf(createMetaByte(SerialDataType.ABSOLUTE_VALUE, 2), 3, 2, 3)

        val testConnector = ConnectorMock()
        testConnector.enable()

        val event = SerialPortEvent(serialPort, SerialPort.LISTENING_EVENT_DATA_RECEIVED, data)

        // When
        serialListener.serialEvent(event)

        assertTrue(testConnector.valueUpdated)
        assertEquals(hardwareDevice.NAME_SLIDER_3, testConnector.valueUpdatedWithKey)
        assertEquals(515, testConnector.valueUpdatedWithValue)
    }

    @Test
    fun testSliderValueCommandReachesConnectorByteByByte() {
        val data = byteArrayOf(createMetaByte(SerialDataType.ABSOLUTE_VALUE, 2), 3, 2, 3)

        val testConnector = ConnectorMock()
        testConnector.enable()

        // When
        sendSerialEventWithData(byteArrayOf(data[0]))
        sendSerialEventWithData(byteArrayOf(data[1], data[2]))
        sendSerialEventWithData(byteArrayOf(data[3]))

        assertTrue(testConnector.valueUpdated)
        assertEquals(hardwareDevice.NAME_SLIDER_3, testConnector.valueUpdatedWithKey)
        assertEquals(515, testConnector.valueUpdatedWithValue)
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
        assertEquals(0, serialListener.receivedData.size)
    }

    @Test
    fun testDataSurroundedWithIncorrectDataGetsProcessed() {
        val data = byteArrayOf(createMetaByte(SerialDataType.ABSOLUTE_VALUE, 2), 3, 2, 3)

        val testConnector = ConnectorMock()
        testConnector.enable()

        // When
        sendSerialEventWithData(byteArrayOf(99))
        sendSerialEventWithData(byteArrayOf(99, data[0]))
        sendSerialEventWithData(byteArrayOf(data[1], data[2]))
        sendSerialEventWithData(byteArrayOf(data[3], 99))
        sendSerialEventWithData(byteArrayOf(99))

        assertTrue(testConnector.valueUpdated)
        assertEquals(hardwareDevice.NAME_SLIDER_3, testConnector.valueUpdatedWithKey)
        assertEquals(515, testConnector.valueUpdatedWithValue)
    }

    @Test
    fun testIncorrectDataGetsProcessedUntilValidData() {
        val data = byteArrayOf(createMetaByte(SerialDataType.ABSOLUTE_VALUE, 2), 3, 2, 3)

        val testConnector = ConnectorMock()
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
        assertEquals(515, testConnector.valueUpdatedWithValue)
    }

    @Test
    fun testDataValueSimilarToMetaByteWillBeTreatedAsValue() {
        val data = byteArrayOf(
            createMetaByte(SerialDataType.ABSOLUTE_VALUE, 2),
            3,
            createMetaByte(SerialDataType.RELATIVE_VALUE, 2),
            3
        )

        val testConnector = ConnectorMock()
        testConnector.enable()

        // When
        sendSerialEventWithData(byteArrayOf(data[0]))
        assertEquals(1, serialListener.receivedData.size)

        sendSerialEventWithData(byteArrayOf(data[1]))
        assertEquals(2, serialListener.receivedData.size)
        assertEquals(data[0].toInt(), serialListener.metaByte)
        assertEquals(2, serialListener.dataLength)

        sendSerialEventWithData(byteArrayOf(data[2]))
        assertEquals(3, serialListener.receivedData.size)
        assertEquals(data[0].toInt(), serialListener.metaByte)
        assertEquals(2, serialListener.dataLength)

        sendSerialEventWithData(byteArrayOf(data[3]))
        assertEquals(0, serialListener.receivedData.size)
        assertNull(serialListener.metaByte)
        assertEquals(2, serialListener.dataLength)

        assertTrue(testConnector.valueUpdated)
        assertEquals(hardwareDevice.NAME_SLIDER_3, testConnector.valueUpdatedWithKey)
        assertEquals(data[2].toUByte().toInt(), (testConnector.valueUpdatedWithValue as Int).shr(8))
        assertEquals(3, (testConnector.valueUpdatedWithValue as Int).and(0xff))
        assertEquals(44547, testConnector.valueUpdatedWithValue)
    }

    @Test
    fun testDataWithIncorrectDataBetween() {
        val data1 = byteArrayOf(createMetaByte(SerialDataType.ABSOLUTE_VALUE, 2), 3, 2, 3)
        val data2 = byteArrayOf(createMetaByte(SerialDataType.RELATIVE_VALUE, 2), 4, 8, 9)

        val testConnector = ConnectorMock()
        testConnector.enable()

        // When
        sendSerialEventWithData(byteArrayOf(99))
        sendSerialEventWithData(byteArrayOf(99, data1[0]))
        sendSerialEventWithData(byteArrayOf(data1[1], 99, data1[2]))
        sendSerialEventWithData(byteArrayOf(99, data1[3], data2[0]))
        sendSerialEventWithData(byteArrayOf(data2[1], data2[2], data2[3], 99))
        sendSerialEventWithData(byteArrayOf(99))

        assertTrue(testConnector.valueUpdated)
        assertEquals(hardwareDevice.NAME_SLIDER_4, testConnector.valueUpdatedWithKey)
        assertEquals(2057, testConnector.valueUpdatedWithValue)
    }

    @Test
    fun testDataWithNonExistingKeyGetsCleared() {
        val data = byteArrayOf(createMetaByte(SerialDataType.ABSOLUTE_VALUE, 2), 99, 2, 3)

        val testConnector = ConnectorMock()
        testConnector.enable()

        // When
        sendSerialEventWithData(byteArrayOf(data[0]))
        sendSerialEventWithData(byteArrayOf(data[1], data[2]))
        sendSerialEventWithData(byteArrayOf(data[3]))
        assertEquals(0, serialListener.receivedData.size)

        assertFalse(testConnector.valueUpdated)
    }

    @Test
    fun testSendSerialUpdate() {
        val data = byteArrayOf(createMetaByte(SerialDataType.UPDATE_REQUEST, 2), 1)

        val testConnector = ConnectorMock()
        testConnector.enable()

        // When
        sendSerialEventWithData(data)

        assertFalse(testConnector.valueUpdated)
        assertNull(serialListener.metaByte)
        assertEquals(0, serialListener.receivedData.size)

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
        return (Config.serialMetaBitsValue.shl(0x05)
                + updateType.id.and(0x07).shl(0x02)
                + dataLength.and(0x03)).toByte()
    }
}