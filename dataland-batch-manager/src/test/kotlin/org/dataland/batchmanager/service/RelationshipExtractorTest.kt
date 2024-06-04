package org.dataland.batchmanager.service

import org.dataland.datalandbatchmanager.service.GleifCsvParser
import org.dataland.datalandbatchmanager.service.RelationshipExtractor
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class RelationshipExtractorTest {

    private var finalMap = mutableMapOf(
        "abcd" to "ghij",
        "bcde" to "ghij",
        "defg" to "ghij",
        "jklm" to "hklm",
    )

    private val testFileContent = """Relationship.StartNode.NodeID, Relationship.EndNode.NodeID, Relationship.RelationshipType
abcd,bcde,IS_DIRECTLY_CONSOLIDATED_BY
bcde,defg,IS_DIRECTLY_CONSOLIDATED_BY
defg,ghij,IS_DIRECTLY_CONSOLIDATED_BY
jklm,hklm,IS_ULTIMATELY_CONSOLIDATED_BY
aa,bb,IS_DIRECTLY_CONSOLIDATED_BY
bb,cc,IS_DIRECTLY_CONSOLIDATED_BY
cc,aa,IS_DIRECTLY_CONSOLIDATED_BY
aaa,bbb,IS_DIRECTLY_CONSOLIDATED_BY
bbb,aaa,IS_DIRECTLY_CONSOLIDATED_BY
bb,cc,IS_FUND-MANAGED_BY
hh,jj,IS_SUBFUND_OF
bb,cc,IS_INTERNATIONAL_BRANCH_OF
bb,cc,IS_FEEDER_TO"""

    @Test
    fun `integration test to see if the mapping returns a final parent mapping as expected`() {
        val relationshipExtractor = RelationshipExtractor()

        val zipBytes = ByteArrayOutputStream()
        val zipStream = ZipOutputStream(zipBytes)
        val buffer = testFileContent.toByteArray()
        zipStream.putNextEntry(ZipEntry("some.csv"))
        zipStream.write(buffer, 0, buffer.size)
        zipStream.closeEntry()
        zipStream.close()
        val zipFile = File("zip.zip")
        zipFile.writeBytes(zipBytes.toByteArray())
        val bufferedReader = GleifCsvParser().getCsvStreamFromZip(zipFile)
        val mappingIterator = GleifCsvParser().readGleifRelationshipDataFromBufferedReader(bufferedReader)

        assert(relationshipExtractor.prepareFinalParentMapping(mappingIterator) == finalMap)
    }
}
