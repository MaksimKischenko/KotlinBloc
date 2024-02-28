package bloc_provider

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.CompletableFuture

object BlocProvider {
    val blocFlow = MutableSharedFlow<Any>()
    fun <T> read(data: T): Flow<T> {
        return flow {
            emit(data)
        }
    }

    suspend fun listenStates(data: Flow<Any>) = coroutineScope {
        data.collect {
            blocFlow.emit(it)
        }
    }
}