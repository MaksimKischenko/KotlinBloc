package event

interface FlightEvent {
    val props: List<Any?>
}

class FlightInit(endPoint: String) : FlightEvent {
    override val props: List<Any?> = listOf(endPoint)
}

