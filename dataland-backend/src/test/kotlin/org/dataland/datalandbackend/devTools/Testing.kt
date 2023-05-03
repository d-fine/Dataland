package org.dataland.datalandbackend.devTools

import org.dataland.datalandbackend.model.lksg.LksgData
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

fun main() {
    val model = LksgData::class
    val propertyList = model.listAllProperties("").toList()
    File("./fqjpList.txt").writeText(propertyList.sorted().joinToString("\n"))
}

fun KClass<*>.listAllProperties(baseName: String): Sequence<String> = sequence {
    if (isTerminal(this@listAllProperties)) {
        yield("$baseName: $simpleName")
    } else {
        val prefix = if (baseName.isBlank()) "" else "$baseName."
        memberProperties.forEach {
            val childBaseName = "$prefix${it.name}"
            val propertyKClass = it.returnType.classifier as? KClass<*>
            yieldAll(propertyKClass!!.listAllProperties(childBaseName))
        }
    }
}

fun isTerminal(propertyClass: KClass<*>): Boolean {
    val isEnum = propertyClass.isSubclassOf(Enum::class)
    val isInDatalandPackage = propertyClass.qualifiedName?.contains("org.dataland") ?: false
    return isEnum || !isInDatalandPackage
}
