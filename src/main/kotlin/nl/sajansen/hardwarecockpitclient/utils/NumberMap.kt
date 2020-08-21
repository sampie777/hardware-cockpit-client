package nl.sajansen.hardwarecockpitclient.utils


enum class NumberMapMode {
    LINEAR,
    ABSOLUTE
}

class NumberMap(
    val minIn: Int,
    val maxIn: Int,
    val minOut: Int,
    val maxOut: Int,
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
            else -> value * (maxIn - minIn) / (maxOut - minOut)
        }
    }

    private fun mapAbsolute(value: Int): Int {
        return when {
            minIn < minOut -> minOut
            maxIn > maxOut -> maxOut
            else -> value
        }
    }
}