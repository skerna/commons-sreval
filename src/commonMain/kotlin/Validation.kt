package io.skerna.reval

import io.skerna.reval.internal.ValidationBuilderImpl


interface Validation<T> {

    companion object {
        operator fun <T> invoke(init: ValidationBuilder<T>.() -> Unit): Validation<T> {
            val builder = ValidationBuilderImpl<T>()
            return builder.apply(init).build()
        }
    }

    fun validate(value: T): ValidationResult<T>


    operator fun invoke(value: T) = validate(value)
}



class Constraint<R> internal constructor(val hint: String, val templateValues: List<String>, val test: (R) -> Boolean){
    companion object {
        fun<R> builder(): ConstraintBuilder<R> {
            return ConstraintBuilder()
        }
    }
    class ConstraintBuilder<R>{
        var hint:String=""
        var templateValues:List<String> = mutableListOf()
        var test: ((R) -> Boolean?)? = null

        fun hint(hint: String) = apply { this.hint = hint }

        fun templateValues(templateValues: List<String>) = apply { this.templateValues = templateValues }

        fun build(test:((R) -> Boolean)): Constraint<R> {
            return Constraint<R>(hint, templateValues, test)
        }
    }
}

