package org.example

import bloc_provider.BlocProvider
import emitter.Emitter
import event.Event
import event.FlightDispose
import event.FlightInit
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onClosed
import kotlinx.coroutines.flow.*
import state.FlightInitial
import state.FlightLoaded
import state.FlightLoading
import java.net.URL
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.milliseconds

private const val BASE_URL = "http://kotlin-book.bignerdranch.com/2e"
private const val FLIGHT_ENDPOINT = "$BASE_URL/flight"
private const val LOYALTY_ENDPOINT = "$BASE_URL/loyalty"

suspend fun main(): Unit = coroutineScope {
    launch {
        BlocProvider.blocFlow.collect { state ->
            when (state) {
                is FlightLoading -> {
                    println("FlightLoading")
                }

                is FlightLoaded -> {
                    println("FlightLoaded")
                    println(state.flight)
                }

                is FlightInitial -> {
                    println("FlightInitial")
                }
            }
        }
    }
    FlightBloc(BlocProvider.read<Event>(FlightInit(FLIGHT_ENDPOINT)))
//    FlightBloc(BlocProvider.read<Event>(FlightDispose()))
//    delay(2000)
}


class FlightBloc() {

    @OptIn(DelicateCoroutinesApi::class)
    constructor(eventFlow: Flow<Event>) : this() {
        GlobalScope.launch {
            BlocProvider.blocFlow.emit(FlightInitial())
            onEvent(eventFlow)
        }
    }

    private suspend fun onEvent(
        eventFlow: Flow<Event>, emitter: Emitter = Emitter()
    ) = coroutineScope {
        emitter.onEachEvent(eventFlow, onEventData = { event ->
            launch {
                if (event is FlightInit) {
                    onFlightsInit(event, emitter)
                } else if (event is FlightDispose) {
                    onFlightsDispose(event, emitter)
                }
            }
        })
    }

    private suspend fun onFlightsInit(
        event: FlightInit, emitter: Emitter
    ) {
        emitter.onEmitState(FlightLoading())
        val result = fetchFlight()
        emitter.onEmitState(
            FlightLoaded(
                result
            )
        )
    }

    private suspend fun onFlightsDispose(
        event: FlightDispose, emitter: Emitter
    ) = coroutineScope {
        emitter.onEmitState(FlightInitial())
    }

    private suspend fun fetchFlight(): String {
        URL(FLIGHT_ENDPOINT).readText()
        val client = HttpClient(CIO)
        val flightResponse = client.get<String>(FLIGHT_ENDPOINT)
        return flightResponse
    }
}



