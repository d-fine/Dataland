package org.dataland.batchmanager.service

import org.dataland.datalandbatchmanager.service.GleifCsvParser
import org.dataland.datalandbatchmanager.service.RelationshipExtractor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class RelationshipExtractorTest {
    private lateinit var csvFile: File

    private var deltaMap = mutableMapOf<String, Set<String>>()

    @BeforeEach
    fun setup() {
        csvFile = File("C:\\Users\\d93414\\Downloads\\20240530-0800-gleif-goldencopy-rr-golden-copy.csv.zip")
    }

    @Test
    fun `test if delta of two files with LEI ISIN mapping looks as expected`() {
        val gleifParser = GleifCsvParser()
        val relationshipExtractor = RelationshipExtractor(gleifParser)
        assert(relationshipExtractor.prepareFinalParentMapping(csvFile) == deltaMap)
    }
}
