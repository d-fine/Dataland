package db.migration

import db.migration.utils.buildDatabaseEntry
import db.migration.utils.mockAndWhenConfigurationForFrameworkMigration
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class V3__MigrateLksgTest {

    @Test
    fun `test that lksg migration script works as expected`() {
        val mockContext = Mockito.mock(Context::class.java)
        mockAndWhenConfigurationForFrameworkMigration(
            mockContext,
            buildOriginalDatabaseEntry(),
            buildExpectedTransformedDatabaseEntry()
        )
        val migration = V3__MigrateLksg()
        migration.migrate(mockContext)
    }

    private fun buildOriginalDatabaseEntry(): String {
        val simplifiedLksgDataset = JSONObject(
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
            "}"
        )
        return buildDatabaseEntry(simplifiedLksgDataset, DataTypeEnum.lksg)
    }

    private fun buildExpectedTransformedDatabaseEntry(): String {
        val simplifiedLksgDataset = JSONObject(
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
            "}"
        )
        return buildDatabaseEntry(simplifiedLksgDataset, DataTypeEnum.lksg)
    }

}