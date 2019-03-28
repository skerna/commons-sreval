package io.skerna.commons.sreval.schema

import io.skerna.commons.sreval.Constraint
import io.skerna.commons.sreval.ValidationBuilder


/**
 * Enum Rules
 * @param T
 */
inline fun <reified T : Enum<T>> ValidationBuilder<String>.enum(): Constraint<String> {
    val enumNames = enumValues<T>().map { it.name }
    return addConstraint(
        "must be one of: {0}",
        enumNames.joinToString("', '", "'", "'")
    ) { it in enumNames }
}
