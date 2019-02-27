import io.skerna.reval.Validation
import io.skerna.reval.ValidationException
import io.skerna.reval.jsonschema.email
import io.skerna.reval.jsonschema.minLength
import kotlin.test.Test
import kotlin.test.assertFailsWith

data class Person(val name:String, val email:String)

class Test{
    @Test
    fun checkResult(){
        val person = Person("Ronald","asdasdasd")

        val validation = Validation<Person>{
            Person::name required {
                minLength(10)
            }
            Person::email required {
                email()
            }
        }


        assertFailsWith(ValidationException::class){
            validation.validate(person).ifContaintsProblemsThrow()
        }
    }
}