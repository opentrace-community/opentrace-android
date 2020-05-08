package au.gov.health.covidsafe.interactor

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.*
import retrofit2.Response
import kotlin.math.pow

private val RETRIES_LIMIT = 3

abstract class UseCase<out Type, in Params>(lifecycle: Lifecycle) : CoroutineScope by MainScope(), LifecycleObserver where Type : Any? {

    private var job: Job = Job()

    init {
        lifecycle.addObserver(this)
    }

    abstract suspend fun run(params: Params): Either<Exception, Type>

    operator fun invoke(params: Params, onSuccess: (Type) -> Unit, onFailure: (Exception) -> Unit) {
        job.cancel()
        job = launch(context = coroutineContext) {
            val result = async(context = Dispatchers.IO) {
                run(params)
            }
            result.await().fold(
                    failed = { onFailure(it) },
                    succeeded = { onSuccess(it) }
            )
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        job.cancel()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        cancel()
    }

    protected suspend fun <S> retryRetrofitCall(call: () -> Response<S>?): Response<S>? {
        var response = call.invoke()
        var retryCount = 0
        while ((response == null || (!response.isSuccessful && response.code() != 403) || response.body() == null) && retryCount < RETRIES_LIMIT) {
            val interval = 2.toDouble().pow(retryCount.toDouble()).toLong() * 1000
            delay(interval)
            response = call.invoke()
            retryCount++
        }
        return response
    }

    protected suspend fun retryOkhttpCall(call: () -> okhttp3.Response?): okhttp3.Response? {
        var response = call.invoke()
        var retryCount = 0
        while ((response == null || !response.isSuccessful || response.body == null) && retryCount < RETRIES_LIMIT) {
            val interval = 2.toDouble().pow(retryCount.toDouble()).toLong() * 1000
            delay(interval)
            response = call.invoke()
            retryCount++
        }
        return if (response != null && response.isSuccessful) {
            response
        } else {
            null

        }
    }

    object None

}