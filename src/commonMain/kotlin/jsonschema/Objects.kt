package io.skerna.reval.jsonschema

import io.skerna.reval.ValidationBuilder


fun <T> ValidationBuilder<T>.const(expected: T) =
    addConstraint(
        "must be {0}",
        expected?.let { "'$it'" } ?: "null"
    ) { expected == it }


