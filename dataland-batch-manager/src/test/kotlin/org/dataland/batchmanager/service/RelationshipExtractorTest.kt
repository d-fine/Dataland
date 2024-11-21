package org.dataland.batchmanager.service

import org.dataland.batchmanager.utils.ZipFileCreator
import org.dataland.datalandbatchmanager.service.CsvParser
import org.dataland.datalandbatchmanager.service.RelationshipExtractor
import org.junit.jupiter.api.Test
import java.io.File

class RelationshipExtractorTest {
    private var finalMap =
        mutableMapOf(
            "abcd" to "ghij",
            "bcde" to "defg",
            "defg" to "ghij",
            "jklm" to "hklm",
            "aa" to "bb",
            "bb" to "cc",
            "cc" to "aa",
            "aaa" to "bbb",
            "bbb" to "aaa",
            "xx" to "yy",
            "hh" to "jj",
        )

    private val testFileContent =
"""Relationship.StartNode.NodeID,Relationship.EndNode.NodeID,Relationship.RelationshipType
abcd,bcde,IS_DIRECTLY_CONSOLIDATED_BY
bcde,defg,IS_DIRECTLY_CONSOLIDATED_BY
defg,ghij,IS_DIRECTLY_CONSOLIDATED_BY
abcd,ghij,IS_ULTIMATELY_CONSOLIDATED_BY
jklm,hklm,IS_ULTIMATELY_CONSOLIDATED_BY
aa,bb,IS_DIRECTLY_CONSOLIDATED_BY
bb,cc,IS_DIRECTLY_CONSOLIDATED_BY
cc,aa,IS_DIRECTLY_CONSOLIDATED_BY
aaa,bbb,IS_DIRECTLY_CONSOLIDATED_BY
bbb,aaa,IS_DIRECTLY_CONSOLIDATED_BY
bb,cc,IS_FUND-MANAGED_BY
xx,yy,IS_FUND-MANAGED_BY
xx,zz,IS_SUBFUND_OF
hh,jj,IS_SUBFUND_OF
bb,cc,IS_INTERNATIONAL_BRANCH_OF
bb,dd,IS_FEEDER_TO"""

    @Test
    fun `integration test to see if the mapping returns a final parent mapping as expected`() {
        val relationshipExtractor = RelationshipExtractor()

        val zipFile = File("zip.zip")
        ZipFileCreator.createZipFile(zipFile, testFileContent)

        val bufferedReader = CsvParser().getCsvStreamFromZip(zipFile)
        val iterable = CsvParser().readGleifRelationshipDataFromBufferedReader(bufferedReader)

        assert(relationshipExtractor.prepareFinalParentMapping(iterable) == finalMap)
    }
}
