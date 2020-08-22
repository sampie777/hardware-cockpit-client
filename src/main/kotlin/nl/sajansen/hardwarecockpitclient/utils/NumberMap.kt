package nl.sajansen.hardwarecockpitclient.utils


enum class NumberMapMode {
    LINEAR,
    ABSOLUTE
}

class NumberMap(
    val minOut: Int,
    val maxOut: Int,
    val minIn: Int = minOut,
    val maxIn: Int = maxOut,
    val mode: NumberMapMode = NumberMapMode.LINEAR
) {
    fun map(value: Int): Int {
        val inputMappedValue = when {
            value < minIn -> minIn
            value > maxIn -> maxIn
            else -> value
        }

        return if (mode == NumberMapMode.ABSOLUTE) {
            mapAbsolute(inputMappedValue)
        } else {
            mapLinear(inputMappedValue)
        }
    }

    private fun mapLinear(value: Int): Int {
        val mappedValue = minOut + ((value - minIn) * (maxOut - minOut).toDouble() / (maxIn - minIn)).toInt()
        return when {
            mappedValue < minOut -> minOut
            mappedValue > maxOut -> maxOut
            else -> mappedValue
        }
    }

    private fun mapAbsolute(value: Int): Int {
        return when {
            value < minOut -> minOut
            value > maxOut -> maxOut
            else -> value
        }
    }
}