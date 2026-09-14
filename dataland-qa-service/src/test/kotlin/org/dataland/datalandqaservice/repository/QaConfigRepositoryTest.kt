package org.dataland.datalandqaservice.repository

import org.assertj.core.api.Assertions.assertThat
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaConfigEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.QaConfigRepository
import org.dataland.datalandqaservice.utils.TestJwtSecurityConfig
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(
    classes = [
        DatalandQaService::class,
        TestJwtSecurityConfig::class,
    ],
    properties = ["spring.profiles.active=nodb"],
)
class QaConfigRepositoryTest {
    @Autowired private lateinit var repository: QaConfigRepository

    @Test
    fun `round trip save and load preserves all map fields`() {
        val config =
            PreApprovalConfig(
                exemptFields = mapOf(DataTypeEnum.sfdr to setOf("fieldA", "fieldB"), DataTypeEnum.lksg to setOf("fieldC")),
                samplingProbability = 0.42,
                decimalRelativeThreshold = 0.3,
                integerAbsoluteThreshold = 7,
                individualDecimalThresholds = mapOf(DataTypeEnum.sfdr to mapOf("fieldA" to 0.1)),
                individualIntegerThresholds = mapOf(DataTypeEnum.sfdr to mapOf("fieldB" to 3L)),
                autoPreApprovalEnabled = false,
                submitUserId = "some-user-id",
            )

        repository.save(QaConfigEntity(config = config))
        val loaded = repository.findById(QaConfigEntity.QA_CONFIG_SINGLETON_ID).orElseThrow().config

        assertThat(loaded).isEqualTo(config)
    }

    @Test
    fun `round trip save and load with empty maps does not error`() {
        val config = PreApprovalConfig()

        repository.save(QaConfigEntity(config = config))
        val loaded = repository.findById(QaConfigEntity.QA_CONFIG_SINGLETON_ID).orElseThrow().config

        assertThat(loaded).isEqualTo(config)
        assertThat(loaded.exemptFields).isEmpty()
        assertThat(loaded.individualDecimalThresholds).isEmpty()
        assertThat(loaded.individualIntegerThresholds).isEmpty()
    }
}
