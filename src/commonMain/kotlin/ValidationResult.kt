package io.skerna.reval

import kotlin.reflect.KProperty1

sealed class ValidationResult<T> {
    abstract operator fun get(vararg propertyPath: Any): List<String>?
    abstract fun <R> map(transform: (T) -> R): ValidationResult<R>

    fun succeeded(): Boolean {
        return this is Valid<T>
    }
    fun failed(): Boolean {
        return this is Invalid<T>
    }

    /**
     * Intenta disparar los errores en caso de error
     * @throws ValidationException if this result failed
     */
    fun ifContaintsProblemsThrow(){
        if(failed()){
            throw ValidationException(this as Invalid<*>)
        }
    }
}

data class Invalid<T>(
    internal val errors: Map<List<String>, List<String>>) : ValidationResult<T>() {

    override fun get(vararg propertyPath: Any): List<String>? = errors[propertyPath.map(::toPathSegment)]

    override fun <R> map(transform: (T) -> R): ValidationResult<R> = Invalid(this.errors)

    private fun toPathSegment(it: Any): String {
        return when (it) {
            is KProperty1<*, *> -> it.name
            else -> it.toString()
        }
    }
    fun getErrors():Map<List<String>, List<String>>{
        return errors
    }
}

data class Valid<T>(val value: T) : ValidationResult<T>() {
    override fun get(vararg propertyPath: Any): List<String>? = null
    override fun <R> map(transform: (T) -> R): ValidationResult<R> = Valid(transform(this.value))

}
