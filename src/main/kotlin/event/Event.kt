package event

import kotlinx.coroutines.Job

interface Event {
    val props: List<Any?>
}

class FlightInit(endPoint: String) : Event {
    override val props: List<Any?> = listOf(endPoint)
}

class FlightDispose() : Event {
//    private val _job = job
//
//    val job: Job
//        get() = _job

    override val props: List<Any?> = emptyList()
}

