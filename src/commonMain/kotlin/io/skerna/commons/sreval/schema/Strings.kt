package io.skerna.commons.sreval.schema

import io.skerna.commons.sreval.Constraint
import io.skerna.commons.sreval.ValidationBuilder

object Regexa{
    val email = "[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])+"
    val guid  = "[\\da-zA-z]{8}-([\\da-zA-z]{4}-){3}[\\da-zA-z]{12}"
}
/**
 *
 */
fun ValidationBuilder<String>.minLength(length: Int): Constraint<String> {
    require(length >= 0) { IllegalArgumentException("minLength requires the length to be >= 0") }
    return addConstraint(
        "must have at least {0} characters",
        length.toString()
    ) { it.length >= length }
}

/**
 * String Max Length
 */
fun ValidationBuilder<String>.maxLength(length: Int): Constraint<String> {
    require(length >= 0) { IllegalArgumentException("maxLength requires the length to be >= 0") }
    return addConstraint(
        "must have at most {0} characters",
        length.toString()
    ) { it.length <= length }
}

/**
 * patter Validation
 */

fun ValidationBuilder<String>.pattern(pattern: String) = pattern(pattern.toRegex())

/**
 * Email Validation
 * rfcardenas@gmail.com = ok
 * rfcardneas = error
 */
fun ValidationBuilder<String>.email() = pattern(Regexa.email) hint ("{0} email invalid")

/**
 * Guid Validations
 */
fun ValidationBuilder<String>.guid() = pattern(Regexa.guid) hint ("{0} guid no valido")

/**
 * Pattern Validation
 */
fun ValidationBuilder<String>.pattern(pattern: Regex) = addConstraint(
    "must match the expected pattern",
    pattern.toString()
) { it.matches(pattern) }

/**
 * Valida que un string forme parte de una lista
 * "A" in "A,B,C,D" = OK
 * "E" in "A,b,C,D" = Error
 */
fun ValidationBuilder<String>.inCollection(strings: List<String>): Constraint<String> {
    return addConstraint(
        "must be in {0}",
        strings.toString()
    ){
        strings.contains(it)
    }
}
