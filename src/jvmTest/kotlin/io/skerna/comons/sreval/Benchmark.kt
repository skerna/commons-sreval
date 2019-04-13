package io.skerna.comons.sreval

import io.skerna.commons.sreval.Validation
import io.skerna.commons.sreval.schema.maxLength
import io.skerna.commons.sreval.schema.maximum
import io.skerna.commons.sreval.schema.minLength
import org.junit.Test
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

/**
 * @author Ronald Cárdenas
 * project: skerna-commons created at 08/04/19
 **/

class Benchmark{
    @Test
    fun benchmark(){
        val benchmark = measureNanoTime {
            for(i in 0..20000){
                val model = SimpleModel(
                        name = "Name $i",
                        emb = Embedded(
                                data = i,
                                data2 = i.toDouble(),
                                data3 = "$i"
                        )
                )
                validation.validate(model)
            }
        }
        println("End in time ${TimeUnit.MILLISECONDS.convert(benchmark,TimeUnit.NANOSECONDS)}")
    }

    data class SimpleModel(
            val name:String,
            val emb:Embedded
    )
    data class Embedded(
            val data:Int,
            val data2:Double,
            val data3:String
    )

    val validation = Validation<SimpleModel>{
        SimpleModel::name{
            maxLength(10000)
        }
        SimpleModel::emb{
            Embedded::data{
                maximum(1000000)
            }
        }
    }

}