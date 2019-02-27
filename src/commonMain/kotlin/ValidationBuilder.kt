package io.skerna.reval

import kotlin.reflect.KProperty1

// FIXME: Remove once JvmName is available in JS projects
expect annotation class JvmName(val name: String)

abstract class ValidationBuilder<T> {
    /**
     * Construye el validador
     */
    abstract fun build(): Validation<T>

    /**
     * Añade una restricción
     * @param errorMessage
     * @param templateValues
     * @param test
     */
    abstract fun addConstraint(errorMessage: String, vararg templateValues: String, test: (T) -> Boolean): Constraint<T>

    /**
     * Añade una mensaje personalizado en caso de error
     * @param hint
     * @return
     */
    abstract infix fun Constraint<T>.hint(hint: String): Constraint<T>

    /**
     * Añade Validaciones, sobre una propiedad de una clase.
     * overload invocation function
     * @param init
     * @return
     */
    abstract operator fun <R> KProperty1<T, R>.invoke(init: ValidationBuilder<R>.() -> Unit)

    /**
     * Añade validaciones para una propiedad de tipo Tipo iterable
     * @param init
     * @param prop
     */
    internal abstract fun <R> onEachIterable(prop: KProperty1<T, Iterable<R>>, init: ValidationBuilder<R>.() -> Unit)

    /**
     * Añade sobre los elementos de tipo iterable
     * @param init
     */
    @JvmName("onEachIterable")
    infix fun <R> KProperty1<T, Iterable<R>>.onEach(init: ValidationBuilder<R>.() -> Unit) = onEachIterable(this, init)

    /**
     * Aplica validaciones sobre los elementos de un Array de tipo R
     * @param init
     * @param prop
     * @param R Type Element Array
     */
    internal abstract fun <R> onEachArray(prop: KProperty1<T, Array<R>>, init: ValidationBuilder<R>.() -> Unit)

    /**
     * Añade validaciones sobre los elementos de un Array
     * @param init
     * @param R --> Type element Array
     */
    @JvmName("onEachArray")
    infix fun <R> KProperty1<T, Array<R>>.onEach(init: ValidationBuilder<R>.() -> Unit) = onEachArray(this, init)

    /**
     * Añade validaciones sobre una propiedad de tipo Mapa
     * @param init
     * @param prop
     * @param K  KeyMap DataType
     * @param V  Value DataType
     */
    internal abstract fun <K, V> onEachMap(prop: KProperty1<T, Map<K, V>>, init: ValidationBuilder<Map.Entry<K, V>>.() -> Unit)

    /**
     * Añade validaciones en las entradas de un mapa
     * @param init
     * @param K  KeyMap DataType
     * @param V  Value DataType
     */
    @JvmName("onEachMap")
    infix fun <K, V> KProperty1<T, Map<K, V>>.onEach(init: ValidationBuilder<Map.Entry<K, V>>.() -> Unit) = onEachMap(this, init)

    /**
     * Especifica el tipo de validacion (Mandatoria u opcional)
     * En este caso la validacion es opcional, si la propiedad no se encuentra seteada,
     * las validaciones no se aplican.
     * null, se aplica la validacion
     * @param init
     * @param R --> Tipo de dato de la propiedad
     */
    abstract infix fun <R> KProperty1<T, R?>.ifPresent(init: ValidationBuilder<R>.() -> Unit)

    /**
     * Especifica el tipo de validacion (Mandatoria u opcional)
     * En este caso la validacion es obligatoria, si la propiedad no se encuentra
     * se emitira un mensaje de Error de validación, marcandola como propiedad requerida
     * @param init
     * @param R --> Tipo de dato de la propiedad
     */
    abstract infix fun <R> KProperty1<T, R?>.required(init: ValidationBuilder<R>.() -> Unit)

    /**
     * Ejecuta una validación, existente, definida fuera del alcance de este Builder
     * @param validation
     */
    abstract fun run(validation: Validation<T>)

    @Deprecated("Elimiar en proximos Releases",level = DeprecationLevel.WARNING)
    abstract val <R> KProperty1<T, R>.has: ValidationBuilder<R>

    /**
     * Añade validaciones dinamicas sobre el objeto principal, pasado en el parametro del Root Validador,
     * por Ejemplo estructuras dinamicas, que no posen atributos de acceso como un Mapa, JsonObect
     * @param init
     * @param key
     * @param self --> funcion, mapea el Objeto a validar y accede a una propiedad personalizada
     */
    abstract fun <R,K> ValidationBuilder<T>.ifPresent(key:K, self: (K, T) -> R?, init: (ValidationBuilder<R>.() -> Unit)?=null)

    /**
     * Añade validaciones dinamicas sobre el objeto principal, pasado en el parametro del Root Validador,
     * por Ejemplo estructuras dinamicas, que no posen atributos de acceso como un Mapa, JsonObect
     * @param init
     * @param key
     * @param self --> funcion, mapea el Objeto a validar y accede a una propiedad personalizada
     */
    abstract fun <R,K> ValidationBuilder<T>.required(key:K, self: (K, T) -> R?, init: (ValidationBuilder<R>.() -> Unit)?=null)
}
