package io.skerna.reval.jsonschema

import io.skerna.reval.Constraint
import io.skerna.reval.ValidationBuilder


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
