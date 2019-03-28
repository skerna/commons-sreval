package io.skerna.commons.sreval.schema

import io.skerna.commons.sreval.ValidationBuilder


/**
 * Rule check if T class is instance of Y class
 */
inline fun <reified T> ValidationBuilder<*>.type() =
    addConstraint(
        "must be of the correct type"
    ) { it is T }

fun <T> ValidationBuilder<T>.enum(vararg allowed: T) =
    addConstraint(
        "must be one of: {0}",
        allowed.joinToString("', '", "'", "'")
    ) { it in allowed }
