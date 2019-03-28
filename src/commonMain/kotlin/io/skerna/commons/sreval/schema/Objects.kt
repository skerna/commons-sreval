package io.skerna.commons.sreval.schema

import io.skerna.commons.sreval.ValidationBuilder


fun <T> ValidationBuilder<T>.const(expected: T) =
    addConstraint(
        "must be {0}",
        expected?.let { "'$it'" } ?: "null"
    ) { expected == it }


