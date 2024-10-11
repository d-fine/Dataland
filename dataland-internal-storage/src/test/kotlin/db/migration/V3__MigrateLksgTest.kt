package db.migration

import db.migration.utils.DataTableEntity
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V3__MigrateLksgTest {
    @Test
    fun `test that lksg migration script works as expected`() {
        val origDatabaseEntry = buildOriginalDatabaseEntry()
        val expectedDataBaseEntry = buildExpectedTransformedDatabaseEntry()
        val migration = V3__MigrateLksg()
        migration.migrateLksgData(origDatabaseEntry)

        Assertions.assertEquals(expectedDataBaseEntry, origDatabaseEntry)
    }

    private fun buildOriginalDatabaseEntry(): DataTableEntity {
        val simplifiedLksgDataset =
            JSONObject(
                "{\"general\":{" +
                    "\"masterData\":{" +
                    "\"dataDate\":\"2023\"," +
                    "\"totalRevenue\":1000" +
                    "}" +
                    "}," +
                    "\"governance\":{" +
                    "\"certificationsPoliciesAndResponsibilities\":{" +
                    "\"codeOfConduct\":\"Yes\"," +
                    "\"codeOfConductTraining\":\"Yes\"" +
                    "}" +
                    "}," +
                    "\"social\":{" +
                    "\"childLabor\":{" +
                    "\"worstFormsOfChildLaborProhibition\":\"Yes\"" +
                    "}" +
                    "}" +
                    "}",
            )
        return DataTableEntity.fromJsonObject("mock-data-id", "lksg", simplifiedLksgDataset)
    }

    private fun buildExpectedTransformedDatabaseEntry(): DataTableEntity {
        val simplifiedLksgDataset =
            JSONObject(
                "{\"general\":{" +
                    "\"masterData\":{" +
                    "\"dataDate\":\"2023\"," +
                    "\"annualTotalRevenue\":1000" +
                    "}" +
                    "}," +
                    "\"governance\":{" +
                    "\"certificationsPoliciesAndResponsibilities\":{" +
                    "\"codeOfConduct\":{" +
                    "\"value\":\"Yes\"" +
                    "}," +
                    "\"codeOfConductTraining\":\"Yes\"" +
                    "}" +
                    "}," +
                    "\"social\":{" +
                    "\"childLabor\":{" +
                    "\"worstFormsOfChildLaborProhibition\":\"Yes\"," +
                    "\"worstFormsOfChildLabor\":\"Yes\"" +
                    "}" +
                    "}" +
                    "}",
            )
        return DataTableEntity.fromJsonObject("mock-data-id", "lksg", simplifiedLksgDataset)
    }
}
