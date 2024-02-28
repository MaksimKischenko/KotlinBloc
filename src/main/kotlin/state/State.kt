package state

interface State {
    val props: List<Any?>
}


class FlightLoading : State {
    override val props: List<Any?> = emptyList()
}

class FlightLoaded(flight: String) : State {

    private val _flight = flight
    val flight: String
        get() = _flight

    override val props: List<Any?> = listOf(flight)
}

class FlightInitial : State {
    override val props: List<Any?> = emptyList()
}
