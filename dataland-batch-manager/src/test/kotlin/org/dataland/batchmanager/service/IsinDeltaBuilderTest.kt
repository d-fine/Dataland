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
        """

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
        """

    @BeforeEach
    fun setup() {
        oldContent.trimIndent()
        newContent.trimIndent()
        deltaMap.put("1000", "1111,1112,1113")
        deltaMap.put("3000", "3333")
        deltaMap.put("4000", " ")
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
        oldFile.deleteOnExit()
        newFile.deleteOnExit()
    }

    @Test
    fun `test if delta of two files with LEI ISIN mapping looks as expected`() {
        val isinDeltaBuilder = IsinDeltaBuilder()
        assert(isinDeltaBuilder.createDeltaOfMappingFile(newFile, oldFile) == (deltaMap))
    }

//    @Test
//    fun `test creation of delta file with incomplete input`() {
//        val newContent = """
//            LEI,ISIN
//            2000,2222
//            3000,3333
//            4000,
//            5000,5555
//            6000,6666
//            6000,6667
//        """.trimIndent()
//        deltaMap.remove("1000")
//        newFile = File("newFile.csv")
//        val printWriter = PrintWriter(newFile)
//        printWriter.println(newContent)
//        printWriter.close()
//        val isinDeltaBuilder = IsinDeltaBuilder()
//        assert(isinDeltaBuilder.createDeltaOfMappingFile(newFile, oldFile).equals(deltaMap))
//    }
}
