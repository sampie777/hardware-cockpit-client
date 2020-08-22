package nl.sajansen.hardwarecockpitclient.utils


enum class NumberMapMode {
    LINEAR,
    ABSOLUTE
}

class NumberMap(
    val minOut: Int,
    val maxOut: Int,
    val minIn: Int = 0,
    val maxIn: Int = 1,
    val mode: NumberMapMode = NumberMapMode.LINEAR
) {
    fun map(value: Int): Int {
        return if (mode == NumberMapMode.ABSOLUTE) {
            mapAbsolute(value)
        } else {
            mapLinear(value)
        }
    }

    private fun mapLinear(value: Int): Int {
        return when {
            minIn < minOut -> minOut
            maxIn > maxOut -> maxOut
            else -> (value * (maxOut - minOut).toDouble() / (maxIn - minIn)).toInt()
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