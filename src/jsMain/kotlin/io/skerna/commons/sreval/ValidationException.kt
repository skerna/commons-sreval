package io.skerna.commons.sreval

actual class ValidationException actual constructor(val invalid: Invalid<*>) : RuntimeException() {

    override val message: String?
        get() = "Validation ended with error codes" +  invalid.getErrors().toString()



    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ValidationException

        if (invalid != other.invalid) return false

        return true
    }

    override fun hashCode(): Int {
        return invalid.hashCode()
    }

}