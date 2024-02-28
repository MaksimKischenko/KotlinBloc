package state

interface FlightState {
    val props: List<Any?>
}


class FlightLoading : FlightState {
    override val props: List<Any?> = emptyList()
}

class FlightLoaded(flight: String) : FlightState {

    private val _flight = flight
    val flight: String
        get() = _flight

    override val props: List<Any?> = listOf(flight)
}
