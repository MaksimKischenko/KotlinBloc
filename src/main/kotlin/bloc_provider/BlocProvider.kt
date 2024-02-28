package bloc_provider

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import state.State


object BlocProvider {
    val blocFlow = MutableSharedFlow<State>()
    fun <T> read(data: T): Flow<T> {
        return flow {
            emit(data)
        }
    }

    suspend fun <T> listenStates(data: Flow<T>) where T : State = coroutineScope {
        data.collect {
            blocFlow.emit(it)
        }
    }
}