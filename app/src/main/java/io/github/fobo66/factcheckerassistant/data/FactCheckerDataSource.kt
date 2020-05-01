package io.github.fobo66.factcheckerassistant.data

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import io.github.fobo66.factcheckerassistant.BuildConfig
import io.github.fobo66.factcheckerassistant.api.FactCheckerApi
import io.github.fobo66.factcheckerassistant.api.models.Claim
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class FactCheckerDataSource(
    private val query: String,
    private val factCheckerApi: FactCheckerApi,
    private val scope: CoroutineScope
) : PageKeyedDataSource<String, Claim>() {

    val errors = MutableLiveData<Throwable>()

    private val errorHandler = CoroutineExceptionHandler { _, error ->
        Timber.e(error, "Error occurred during loading claims for query %s", query)
        errors.postValue(error)
    }

    private var nextPageToken: String? = null

    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<String, Claim>
    ) {
        scope.launch(errorHandler) {
            Timber.d("Loading claims started")
            val result = factCheckerApi.search(
                query,
                pageSize = params.requestedLoadSize,
                key = BuildConfig.API_KEY
            )
            Timber.d("Loading claims finished")
            nextPageToken = result.nextPageToken
            callback.onResult(result.claims, null, nextPageToken)
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, Claim>) {
        scope.launch(errorHandler) {
            Timber.d("Loading more claims started")
            val result = factCheckerApi.search(
                query,
                pageSize = params.requestedLoadSize,
                pageToken = nextPageToken,
                key = BuildConfig.API_KEY
            )
            Timber.d("Loading more claims finished")
            nextPageToken = result.nextPageToken
            callback.onResult(result.claims, nextPageToken)
        }
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, Claim>) {
        // do nothing
    }
}