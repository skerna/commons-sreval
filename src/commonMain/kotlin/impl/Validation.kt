package io.skerna.reval.internal

import io.skerna.reval.Constraint
import io.skerna.reval.Invalid
import io.skerna.reval.Valid
import io.skerna.reval.Validation
import io.skerna.reval.ValidationResult
import kotlin.reflect.KProperty1

internal class NonNullPropertyValidation<T, R>(
    private val property: KProperty1<T, R>,
    private val validation: Validation<R>
) : Validation<T> {
    override fun validate(value: T): ValidationResult<T> {
        val propertyValue = property(value)
        return validation(propertyValue).mapError { listOf(property.name) + it }.map { value }
    }
}

internal class OptionalPropertyValidation<T, R>(
    private val property: KProperty1<T, R?>,
    private val validation: Validation<R>
) : Validation<T> {
    override fun validate(value: T): ValidationResult<T> {
        val propertyValue = property(value) ?: return Valid(value)
        return validation(propertyValue).mapError { listOf(property.name) + it }.map { value }
    }
}

internal class RequiredPropertyValidation<T, R>(
    private val property: KProperty1<T, R?>,
    private val validation: Validation<R>
) : Validation<T> {
    override fun validate(value: T): ValidationResult<T> {
        val propertyValue = property(value)
            ?: return Invalid<T>(mapOf(listOf(property.name) to listOf("is required")))
        return validation(propertyValue).mapError { listOf(property.name) + it }.map { value }
    }
}


internal class NonNullSelfPropertyValidation<T, R,K>(
    private val name:K,
    private val validation: Validation<R>,
    private val self:(K,T)->R
) : Validation<T> {
    val keyAsStr = name.toString()

    override fun validate(value: T): ValidationResult<T> {
        val propertyValue = self(name,value)
        if(propertyValue == null){
            return Invalid<T>(mapOf(listOf(keyAsStr) to listOf("property: [$keyAsStr] not found")))
        }
        return validation(propertyValue).mapError { listOf(keyAsStr) + it }.map { value }
    }
}

internal class OptionalSelfValidation<T, R,K>(
    private val property: K,
    private val validation: Validation<R>,
    private val self:(K,T)->R
) : Validation<T> {
    val keyAsStr = property.toString()

    override fun validate(value: T): ValidationResult<T> {
        val valSelft = self(property,value)
        val propertyValue = valSelft ?: return Valid(value)
        return validation(propertyValue).mapError { listOf(keyAsStr) + it }.map { value }
    }
}

internal class RequiredSelfValidation<T, R,K>(
    private val property: K,
    private val validation: Validation<R>,
    private val self:(K,T)->R
) : Validation<T> {
    val keyAsStr = property.toString()

    override fun validate(value: T): ValidationResult<T> {
        val valSelft = self(property,value)
        val propertyValue = valSelft
            ?: return Invalid(mapOf(listOf(keyAsStr) to listOf("property: [$keyAsStr] not found")))
        return validation(propertyValue).mapError { listOf(keyAsStr) + it }.map { value }
    }
}


internal class IterableValidation<T>(
    private val validation: Validation<T>
) : Validation<Iterable<T>> {

    override fun validate(value: Iterable<T>): ValidationResult<Iterable<T>> {
        return value.foldIndexed(Valid(value)) { index, result: ValidationResult<Iterable<T>>, propertyValue ->
            val propertyValidation = validation(propertyValue).mapError { listOf(index.toString()) + it }.map { value }
            result.combineWith(propertyValidation)
        }

    }
}

internal class ArrayValidation<T>(
    private val validation: Validation<T>
) : Validation<Array<T>> {
    override fun validate(value: Array<T>): ValidationResult<Array<T>> {
        return value.foldIndexed(Valid(value)) { index, result: ValidationResult<Array<T>>, propertyValue ->
            val propertyValidation = validation(propertyValue).mapError { listOf(index.toString()) + it }.map { value }
            result.combineWith(propertyValidation)
        }

    }
}

internal class MapValidation<K, V>(
    private val validation: Validation<Map.Entry<K, V>>
) : Validation<Map<K, V>> {
    override fun validate(value: Map<K, V>): ValidationResult<Map<K, V>> {
        return value.asSequence().fold(Valid(value)) { result: ValidationResult<Map<K, V>>, entry ->
            val propertyValidation = validation(entry).mapError { listOf(entry.key.toString()) + it.drop(1) }.map { value }
            result.combineWith(propertyValidation)
        }

    }
}

internal class ValidationNode<T>(
    private val constraints: List<Constraint<T>>,
    private val subValidations: List<Validation<T>>
) : Validation<T> {
    override fun validate(value: T): ValidationResult<T> {
        val subValidationResult = applySubValidations(value, keyTransform = { it })
        val localValidationResult = localValidation(value)
        return localValidationResult.combineWith(subValidationResult)
    }

    private fun localValidation(value: T): ValidationResult<T> {
        return constraints
            .filter { !it.test(value) }
            .map { constructHint(value, it) }
            .let { errors ->
                if (errors.isEmpty()) {
                    Valid(value)
                } else {
                    Invalid(mapOf(emptyList<String>() to errors))
                }
            }
    }

    private fun constructHint(value: T, it: Constraint<T>): String {
        val replaceValue = it.hint.replace("{value}", value.toString())
        return it.templateValues
            .foldIndexed(replaceValue) { index, hint, templateValue -> hint.replace("{$index}", templateValue) }
    }

    private fun applySubValidations(propertyValue: T, keyTransform: (List<String>) -> List<String>): ValidationResult<T> {
        return subValidations.fold(Valid(propertyValue)) { existingValidation: ValidationResult<T>, validation ->
            val newValidation = validation.validate(propertyValue).mapError(keyTransform)
            existingValidation.combineWith(newValidation)
        }
    }
}

internal fun <R> ValidationResult<R>.mapError(keyTransform: (List<String>) -> List<String>): ValidationResult<R> {
    return when (this) {
        is Valid -> this
        is Invalid -> Invalid(this.errors.mapKeys { (key, _) ->
            keyTransform(key)
        })
    }
}

internal fun <R> ValidationResult<R>.combineWith(other: ValidationResult<R>): ValidationResult<R> {
    return when (this) {
        is Valid -> return other
        is Invalid -> when (other) {
            is Valid -> this
            is Invalid -> {
                Invalid((this.errors.toList() + other.errors.toList())
                    .groupBy({ it.first }, { it.second })
                    .mapValues { (_, values) -> values.flatten() })
            }
        }
    }
}
