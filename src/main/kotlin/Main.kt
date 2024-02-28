package org.example

import bloc_provider.BlocProvider
import emitter.Emitter
import event.FlightEvent
import event.FlightInit
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import state.FlightLoaded
import state.FlightLoading
import java.net.URL

private const val BASE_URL = "http://kotlin-book.bignerdranch.com/2e"
private const val FLIGHT_ENDPOINT = "$BASE_URL/flight"
private const val LOYALTY_ENDPOINT = "$BASE_URL/loyalty"


suspend fun main(): Unit = coroutineScope {
    FlightBloc(BlocProvider.read<FlightEvent>(FlightInit(FLIGHT_ENDPOINT)))
    val job = launch {
        BlocProvider.blocFlow.collect {
            println(it)
        }
    }
    delay(20000)
    job.cancel()
}


class FlightBloc() {
    @OptIn(DelicateCoroutinesApi::class)
    constructor(eventFlow: Flow<FlightEvent>) : this() {
        GlobalScope.launch {
            onEvent(eventFlow)
        }
    }

    private suspend fun onEvent(
        eventFlow: Flow<FlightEvent>, emitter: Emitter = Emitter()
    ) = coroutineScope {
        emitter.onEachEvent(eventFlow, onEventData = {
            if (it is FlightInit) {
                launch {
                    onFlightsInit(it, emitter)
                }
            }
        })
    }

    private suspend fun onFlightsInit(
        event: FlightInit, emitter: Emitter
    )  = coroutineScope{
        emitter.onEmitState(FlightLoading())
        val result = fetchFlight()
        println("final: $result")
        emitter.onEmitState(FlightLoaded(
            result
        ))
    }

    private suspend fun fetchFlight():String = coroutineScope {
        var result = ""
        launch(Dispatchers.IO) {
            URL(FLIGHT_ENDPOINT).readText()
            val client = HttpClient(CIO)
            val flightResponse = client.get<String>(FLIGHT_ENDPOINT)
            result = flightResponse
        }.join()
        return@coroutineScope result
    }
}



