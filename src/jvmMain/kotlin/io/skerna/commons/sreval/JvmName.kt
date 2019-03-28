package io.skerna.commons.sreval

import io.skerna.commons.sreval.schema.flexRequired
import io.skerna.commons.sreval.schema.inCollection


// FIXME: Remove once JvmName is available in JS projects
actual typealias JvmName = kotlin.jvm.JvmName

data class Person(val name: String? = "asdasd")

fun main(vararg args:String){
    var map = mutableMapOf<String,String>()

    val a = Validation<MutableMap<String, String>> {
        flexRequired("a") { k, s -> s[k] }
    }
    val r = a.validate(mutableMapOf("asdas" to "adssad"))
    println(r)

    val validation = Validation<Person> {
        Person::name required {
            inCollection(listOf("Ronald", "Francisco")) hint "No valido"
        }
    }

}
