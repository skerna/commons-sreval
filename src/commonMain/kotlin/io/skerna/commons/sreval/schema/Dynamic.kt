package io.skerna.commons.sreval.schema

import io.skerna.commons.sreval.Constraint
import io.skerna.commons.sreval.ValidationBuilder

/**
 * Flex Validacion, define una regla de tipo mandatory, marcando un campo como requerido
 * @param key
 * @param test  * (key:String, test: (String, T) -> R)
 * @param R --> property
 * @param T --- Parent Type
 * test funcion, toma como primer parametro la clave y el segundo el store del cual
 * extraer el valor para verificar que exista.
 */
fun <T,R> ValidationBuilder<T>.flexRequired(key:String, test: (String, T) -> R): Constraint<T> =addConstraint(
    "Element '{0}' is required",
    key
) {
    val r = test.invoke(key,it)
    r != null
}
