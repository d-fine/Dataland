package org.dataland.datalandspecificationservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackendutils.utils.JsonComparator
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.io.File
import java.util.stream.Stream

const val FRAMEWORK = "sfdr"
const val DIRECTORY_BEFORE = "$FRAMEWORK/before/"
const val DIRECTORY_AFTER = "$FRAMEWORK/after/"

class DataMigrationTest {
    private val objectMapper = ObjectMapper()

    @ParameterizedTest
    @ArgumentsSource(JsonFileProvider::class)
    fun `test migration`(
        fileBefore: File,
        fileAfter: File,
    ) {
        val fileName = fileBefore.name
        require(fileAfter.exists() && fileAfter.isFile) {
            "File $fileName does not exist in directory $DIRECTORY_AFTER."
        }
        val diffList =
            JsonComparator.compareJson(
                objectMapper.readTree(fileBefore),
                objectMapper.readTree(fileAfter),
                JsonComparator.JsonComparisonOptions(
                    ignoredKeys = setOf("referencedReports", "publicationDate"),
                    fullyNullObjectsAreEqualToNull = true,
                ),
            )
        require(diffList.isEmpty()) {
            "A difference was detected in the files with name $fileName:\n" +
                "$diffList"
        }
    }
}

class JsonFileProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments?>? {
        val argumentsList = mutableListOf<Arguments>()
        File(javaClass.classLoader.getResource(DIRECTORY_BEFORE)!!.toURI()).listFiles()!!.forEach { fileBefore ->
            val fileName = fileBefore.name
            val fileAfter = File(javaClass.classLoader.getResource("$DIRECTORY_AFTER/$fileName")!!.toURI())
            val arguments = Arguments.of(fileBefore, fileAfter)
            argumentsList.add(arguments)
        }
        return argumentsList.stream()
    }
}
