package io.skerna.reval.jsonschema

import io.skerna.reval.Constraint
import io.skerna.reval.ValidationBuilder

inline fun <reified T: Map<*, *>> ValidationBuilder<T>.minProperties(minSize: Int): Constraint<T> =
    minItems(minSize) hint "must have at least {0} properties"

inline fun <reified T: Map<*, *>> ValidationBuilder<T>.maxProperties(maxSize: Int): Constraint<T> =
    maxItems(maxSize) hint "must have at most {0} properties"
inline fun <reified T: Map<*,*>> ValidationBuilder<T>.requiereKeys(keys: List<String>): Constraint<T> = addConstraint("must have at most $keys",keys.toString()) {
    var result:Boolean = true
    for (key in keys) {
        if(!it.containsKey(key)){
            result = false
            break
        }
    }
    result
}
