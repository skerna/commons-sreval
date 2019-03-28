package io.skerna.commons.sreval.schema

import io.skerna.commons.sreval.Constraint
import io.skerna.commons.sreval.ValidationBuilder


inline fun <reified T> ValidationBuilder<T>.minItems(minSize: Int): Constraint<T> = addConstraint(
    "must have at least {0} items",
    minSize.toString()
) {
    when (it) {
        is Iterable<*> -> it.count() >= minSize
        is Array<*> -> it.count() >= minSize
        is Map<*, *> -> it.count() >= minSize
        else -> throw IllegalStateException("minItems can not be applied to type ${T::class}")
    }
}


inline fun <reified T> ValidationBuilder<T>.maxItems(maxSize: Int): Constraint<T> = addConstraint(
    "must have at most {0} items",
    maxSize.toString()
) {
    when (it) {
        is Iterable<*> -> it.count() <= maxSize
        is Array<*> -> it.count() <= maxSize
        is Map<*, *> -> it.count() <= maxSize
        else -> throw IllegalStateException("maxItems can not be applied to type ${T::class}")
    }
}

/**
 * Verifica valores unicos
 */
inline fun <reified T> ValidationBuilder<T>.uniqueItems(unique: Boolean): Constraint<T> = addConstraint(
    "all items must be unique"
) {
    !unique || when (it) {
        is Iterable<*> -> it.distinct().count() == it.count()
        is Array<*> -> it.distinct().count() == it.count()
        else -> throw IllegalStateException("uniqueItems can not be applied to type ${T::class}")
    }
}


