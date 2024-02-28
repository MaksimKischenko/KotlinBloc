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
import kotlinx.coroutines.flow.*
import state.FlightInitial
import state.FlightLoaded
import state.FlightLoading
import java.net.URL

private const val BASE_URL = "http://kotlin-book.bignerdranch.com/2e"
private const val FLIGHT_ENDPOINT = "$BASE_URL/flight"
private const val LOYALTY_ENDPOINT = "$BASE_URL/loyalty"


suspend fun main(): Unit = coroutineScope {
    launch {
        BlocProvider.blocFlow.collect {
            println(it)
        }
    }
    FlightBloc(BlocProvider.read<Event>(FlightInit(FLIGHT_ENDPOINT)))
    delay(2000)
    FlightBloc(BlocProvider.read<Event>(FlightDispose()))
    delay(2000)
}


class FlightBloc() {
    @OptIn(DelicateCoroutinesApi::class)
    constructor(eventFlow: Flow<Event>) : this() {
        GlobalScope.launch {
            onEvent(eventFlow)
        }
    }

    val jobDeferred = CompletableDeferred<Job>()
    val jobChannel = Channel<Job>()

    private suspend fun onEvent(
        eventFlow: Flow<Event>, emitter: Emitter = Emitter()
    ) = coroutineScope {
        emitter.onEachEvent(eventFlow, onEventData = { event ->
            if (event is FlightInit) {
                launch {
                    println(Thread.currentThread().name)
                    onFlightsInit(event, emitter)
                }
            } else if (event is FlightDispose) {
                launch {
                    println(Thread.currentThread().name)
                    onFlightsDispose(event, emitter)
                }
            }
        })
    }

    private suspend fun onFlightsInit(
        event: FlightInit, emitter: Emitter
    ) = coroutineScope {
        emitter.onEmitState(FlightLoading())
        val result = fetchFlight()
        println("final: $result")
        emitter.onEmitState(
            FlightLoaded(
                result
            )
        )
    }

    private suspend fun onFlightsDispose(
        event: FlightDispose, emitter: Emitter
    ) = coroutineScope {
        println("onFlightsDispose")

        val job = jobDeferred.await()

        println("JOB:$job")
        job.cancel()
        if (job.isCancelled) {
            emitter.onEmitState(FlightInitial())
        }
    }


    private suspend fun fetchFlight(): String = coroutineScope {
        var result = ""
        val job = launch(Dispatchers.IO) {
            URL(FLIGHT_ENDPOINT).readText()
            val client = HttpClient(CIO)
            val flightResponse = client.get<String>(FLIGHT_ENDPOINT)
            result = flightResponse
        }
        jobDeferred.complete(job)
        job.join()
        return@coroutineScope result
    }
}



