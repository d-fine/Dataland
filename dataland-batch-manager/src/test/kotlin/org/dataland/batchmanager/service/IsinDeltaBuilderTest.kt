package org.dataland.batchmanager.service

import org.dataland.datalandbatchmanager.service.IsinDeltaBuilder
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.io.PrintWriter

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IsinDeltaBuilderTest {

    private lateinit var oldFile: File
    private lateinit var newFile: File
    private var deltaMap = mutableMapOf<String, String>()

    @BeforeEach
    fun setup() {
        val oldContent = """
            LEI,ISIN
            1000,1111
            1000,1112
            2000,2222
            3000,3333
            3000,3334
            4000,4444
            6000,6666
            6000,6667
        """.trimIndent()
        val newContent = """
            LEI,ISIN
            1000,1111
            1000,1112
            1000,1113
            2000,2222
            3000,3333
            4000, 
            5000,5555
            6000,6666
            6000,6667
        """.trimIndent()

        deltaMap.put("1000", "1111,1112,1113")
        deltaMap.put("3000", "3333")
        deltaMap.put("4000", "")
        deltaMap.put("5000", "5555")
//        create file oldFile
        oldFile = File("oldFile.csv")
        var printWriter = PrintWriter(oldFile)
        printWriter.println(oldContent)
        printWriter.close()
//        create file newFile
        newFile = File("newFile.csv")
        printWriter = PrintWriter(newFile)
        printWriter.println(newContent)
        printWriter.close()
    }

    @AfterAll
    fun cleanup() {
        oldFile.delete()
        newFile.delete()
    }

    @Test
    fun `test if delta of two files with LEI-ISIN mapping looks as expected`() {
        val tmpFile = File("tmp")
        val isinDeltaBuilder = IsinDeltaBuilder(tmpFile)
//        println("generated" + isinDeltaBuilder.createDeltaOfMappingFile(oldFile, newFile))
//        println("compared to$deltaMap")
        assert(isinDeltaBuilder.createDeltaOfMappingFile(oldFile, newFile).equals(deltaMap))
        tmpFile.delete()
    }

    @Test
    fun `test if new file moves in place of old file`() {
        val newLines: List<String> = File(newFile.toString()).useLines { lines -> lines.take(5).toList() }
        val isinDeltaBuilder = IsinDeltaBuilder(oldFile)
        isinDeltaBuilder.replaceOldMappingFile(newFile)
        assert(!File("newFile.csv").exists())
        assert(File("oldFile.csv").exists())

        val movedLines: List<String> = File(oldFile.toString()).useLines { lines -> lines.take(5).toList() }
        assert(movedLines.hashCode().equals(newLines.hashCode()))
    }
}
