package io.skerna.commons.sreval.impl

import io.skerna.commons.sreval.Constraint
import io.skerna.commons.sreval.Validation
import io.skerna.commons.sreval.ValidationBuilder
import io.skerna.commons.sreval.impl.ValidationBuilderImpl.Companion.PropModifier.*
import kotlin.collections.Map.Entry
import kotlin.reflect.KProperty1

internal class ValidationBuilderImpl<T> : ValidationBuilder<T>() {
    companion object {
        private enum class PropModifier {
            NonNull, Optional, OptionalRequired
        }

        private abstract class PropKey<T> {
            abstract fun build(builder: ValidationBuilderImpl<*>): Validation<T>
        }

        private data class SingleValuePropKey<T, R>(
            val property: KProperty1<T, R>,
            val modifier: PropModifier
        ) : PropKey<T>() {
            override fun build(builder: ValidationBuilderImpl<*>): Validation<T> {
                @Suppress("UNCHECKED_CAST")
                val validations = (builder as ValidationBuilderImpl<R>).build()
                return when (modifier) {
                    NonNull -> NonNullPropertyValidation(property, validations)
                    Optional -> OptionalPropertyValidation(property, validations)
                    OptionalRequired -> RequiredPropertyValidation(property, validations)
                }
            }
        }

        private data class IterablePropKey<T, R>(
            val property: KProperty1<T, Iterable<R>>,
            val modifier: PropModifier
        ) : PropKey<T>() {
            override fun build(builder: ValidationBuilderImpl<*>): Validation<T> {
                @Suppress("UNCHECKED_CAST")
                val validations = (builder as ValidationBuilderImpl<R>).build()
                return when (modifier) {
                    NonNull -> NonNullPropertyValidation(property, IterableValidation(validations))
                    Optional -> OptionalPropertyValidation(property, IterableValidation(validations))
                    OptionalRequired -> RequiredPropertyValidation(property, IterableValidation(validations))
                }
            }
        }

        private data class ArrayPropKey<T, R>(
            val property: KProperty1<T, Array<R>>,
            val modifier: PropModifier
        ) : PropKey<T>() {
            override fun build(builder: ValidationBuilderImpl<*>): Validation<T> {
                @Suppress("UNCHECKED_CAST")
                val validations = (builder as ValidationBuilderImpl<R>).build()
                return when (modifier) {
                    NonNull -> NonNullPropertyValidation(property, ArrayValidation(validations))
                    Optional -> OptionalPropertyValidation(property, ArrayValidation(validations))
                    OptionalRequired -> RequiredPropertyValidation(property, ArrayValidation(validations))
                }
            }
        }

        private data class MapPropKey<T, K, V>(
            val property: KProperty1<T, Map<K, V>>,
            val modifier: PropModifier
        ) : PropKey<T>() {
            override fun build(builder: ValidationBuilderImpl<*>): Validation<T> {
                @Suppress("UNCHECKED_CAST")
                val validations = (builder as ValidationBuilderImpl<Entry<K, V>>).build()
                return when (modifier) {
                    NonNull -> NonNullPropertyValidation(property, MapValidation(validations))
                    Optional -> OptionalPropertyValidation(property, MapValidation(validations))
                    OptionalRequired -> RequiredPropertyValidation(property, MapValidation(validations))
                }
            }
        }

        private  class SelfValuePropKey<T, R,K>(
            val property: K,
            val modifier: PropModifier,
            val self:(K,T)->R
        ) : PropKey<T>() {
            val key = property.toString()
            override fun build(builder: ValidationBuilderImpl<*>): Validation<T> {
                @Suppress("UNCHECKED_CAST")
                val validations = (builder as ValidationBuilderImpl<R>).build()
                return when (modifier) {
                    NonNull -> NonNullSelfPropertyValidation(property, validations, self)
                    Optional -> OptionalSelfValidation(property, validations, self)
                    OptionalRequired -> RequiredSelfValidation(property, validations, self)
                }
            }
            override fun equals(other: Any?): Boolean {
                return if(other == null) false else other.equals(key)
            }

            override fun hashCode(): Int {
                return key.hashCode()
            }

            override fun toString(): String {
                return key.toString()
            }
        }
    }

    private val constraints = mutableListOf<Constraint<T>>()
    private val subValidations = mutableMapOf<PropKey<T>, ValidationBuilderImpl<*>>()
    private val prebuiltValidations = mutableListOf<Validation<T>>()

    override fun Constraint<T>.hint(hint: String): Constraint<T> =
        Constraint(hint, this.templateValues, this.test).also { constraints.remove(this); constraints.add(it) }

    override fun addConstraint(errorMessage: String, vararg templateValues: String, test: (T) -> Boolean): Constraint<T> {
        return Constraint(errorMessage, templateValues.toList(), test).also { constraints.add(it) }
    }

    private fun <R> KProperty1<T, R?>.getOrCreateBuilder(modifier: PropModifier): ValidationBuilder<R> {
        val key = SingleValuePropKey(this, modifier)
        @Suppress("UNCHECKED_CAST")
        return (subValidations.getOrPut(key, { ValidationBuilderImpl<R>() }) as ValidationBuilder<R>)
    }

    private fun <R> KProperty1<T, Iterable<R>>.getOrCreateIterablePropertyBuilder(modifier: PropModifier): ValidationBuilder<R> {
        val key = IterablePropKey(this, modifier)
        @Suppress("UNCHECKED_CAST")
        return (subValidations.getOrPut(key, { ValidationBuilderImpl<R>() }) as ValidationBuilder<R>)
    }

    private fun <R> PropKey<T>.getOrCreateBuilder(): ValidationBuilder<R> {
        @Suppress("UNCHECKED_CAST")
        return (subValidations.getOrPut(this, { ValidationBuilderImpl<R>() }) as ValidationBuilder<R>)
    }

    override fun <R> KProperty1<T, R>.invoke(init: ValidationBuilder<R>.() -> Unit) {
        getOrCreateBuilder(NonNull).also(init)
    }

    override fun <R> onEachIterable(prop: KProperty1<T, Iterable<R>>, init: ValidationBuilder<R>.() -> Unit) {
        prop.getOrCreateIterablePropertyBuilder(NonNull).also(init)
    }

    override fun <R> onEachArray(prop: KProperty1<T, Array<R>>, init: ValidationBuilder<R>.() -> Unit) {
        ArrayPropKey(prop, NonNull).getOrCreateBuilder<R>().also(init)
    }

    override fun <K, V> onEachMap(prop: KProperty1<T, Map<K, V>>, init: ValidationBuilder<Entry<K, V>>.() -> Unit) {
        MapPropKey(prop, NonNull).getOrCreateBuilder<Map.Entry<K, V>>().also(init)
    }

    override fun <R> KProperty1<T, R?>.ifPresent(init: ValidationBuilder<R>.() -> Unit) {
        getOrCreateBuilder(Optional).also(init)
    }

    override fun <R> KProperty1<T, R?>.required(init: ValidationBuilder<R>.() -> Unit) {
        getOrCreateBuilder(OptionalRequired).also(init)
    }

    override val <R> KProperty1<T, R>.has: ValidationBuilder<R>
        get() = getOrCreateBuilder(NonNull)


    override fun run(validation: Validation<T>) {
        prebuiltValidations.add(validation)
    }

    override fun build(): Validation<T> {
        val nestedValidations = subValidations.map { (key, builder) ->
            key.build(builder)
        }
        return ValidationNode(constraints, nestedValidations + prebuiltValidations)
    }

    private fun <R,K> generateSelfBuilder(identifier:K, modifier: PropModifier, self: (K, T) ->R?): ValidationBuilder<R> {
        val key = SelfValuePropKey(identifier, modifier, self)
        @Suppress("UNCHECKED_CAST")

        return (subValidations.getOrPut(key) { ValidationBuilderImpl<R>() } as ValidationBuilder<R>)
    }



    private fun<R,K> self(ident:K, self:(K,T)->R?, modifier: PropModifier, init: (ValidationBuilder<R>.() -> Unit)?){
        if(init == null){
            generateSelfBuilder(ident,modifier,self)
        }else{
            init.invoke(generateSelfBuilder(ident,modifier,self))
        }
    }

    override fun <R,K> ValidationBuilder<T>.ifPresent(key:K, self: (K, T) -> R?, init: (ValidationBuilder<R>.() -> Unit)?){
        self(key,self,Optional,init)
    }

    override fun <R,K> ValidationBuilder<T>.required(key:K, self: (K, T) -> R?, init: (ValidationBuilder<R>.() -> Unit)?){
        self(key,self,NonNull,init)
    }
}


