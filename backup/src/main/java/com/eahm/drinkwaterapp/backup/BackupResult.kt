package com.eahm.drinkwaterapp.backup

sealed class BackupResult<out T> {

    data class Success<out T>(val value: T) : BackupResult<T>()

    data class Failure(val error: BackupError) : BackupResult<Nothing>()

    fun getOrElse(default: @UnsafeVariance T): T = when (this) {
        is Success -> value
        is Failure -> default
    }

    fun <R> map(transform: (T) -> R): BackupResult<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure
}

inline fun <T> BackupResult<T>.onSuccess(action: (T) -> Unit): BackupResult<T> {
    if (this is BackupResult.Success) action(value)
    return this
}

inline fun <T> BackupResult<T>.onFailure(action: (BackupError) -> Unit): BackupResult<T> {
    if (this is BackupResult.Failure) action(error)
    return this
}
