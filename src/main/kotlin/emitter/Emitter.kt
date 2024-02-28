package emitter

import bloc_provider.BlocProvider
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import state.State


class Emitter {
    suspend fun <T> onEachEvent(
        stream: Flow<T>,
        onEventData: (data: T) -> Unit,
//        onError: ((error: Any, stackTrace: StackTraceElement?) -> Unit)
    ) = coroutineScope {
        try {
            stream.collect() {
                onEventData.invoke(it)
            }
        } catch (e: Exception) {
//            onError.invoke(e, e.stackTrace.firstOrNull())
        }

    }

    suspend fun <T> onEmitState(data: T )where T : State {
        BlocProvider.listenStates(flow{
            emit(data)
        })
    }
}