package org.dataland.datalandbackend.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.controller.IsinLeiDataController
import org.dataland.datalandbackend.entities.IsinLeiEntity
import org.dataland.datalandbackend.model.IsinLeiMappingData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class IsinLeiDataControllerTest(
    @Autowired private val isinLeiManager: IsinLeiManager,
) {
    @PersistenceContext
    private lateinit var entityManager: EntityManager

    private var objectMapper = jacksonObjectMapper()

    val entity1 = IsinLeiEntity("123", "LEI123")
    val entity2 = IsinLeiEntity("456", "LEI456")
    val jsonPayload =
        """
        [   
            {
                "isin": "123456789",
                "lei": "LEI123456789"
            },
            {
                "isin": "987654321",
                "lei": "LEI987654321"
            }
        ]
        """.trimIndent()

    lateinit var isinLeiDataController: IsinLeiDataController

    @BeforeEach
    fun initCompanyController() {
        isinLeiDataController =
            IsinLeiDataController(
                isinLeiManager,
            )
    }

    fun putIsinLeiData(json: String) {
        isinLeiDataController.putIsinLeiMapping(
            objectMapper.readValue(
                json,
                objectMapper.typeFactory.constructCollectionType(List::class.java, IsinLeiMappingData::class.java),
            ),
        )
    }

    @Test
    fun `add sample ISIN LEI mapping to empty database and check if it is there`() {
        putIsinLeiData(jsonPayload)

        val query =
            entityManager.createQuery(
                "SELECT i FROM IsinLeiEntity i", IsinLeiEntity::class.java,
            )
        val result = query.resultList
        assertEquals(result.size, 2)
        assertEquals(result[0].isin, "123456789")
    }

    @Test
    fun `add sample ISIN LEI mapping to database and check if it replaced the old data`() {
        entityManager.persist(entity1)
        entityManager.persist(entity2)
        entityManager.flush()

        putIsinLeiData(jsonPayload)

        val query =
            entityManager.createQuery(
                "SELECT i FROM IsinLeiEntity i", IsinLeiEntity::class.java,
            )
        val result = query.resultList
        assertEquals(result.size, 2)
        assertEquals(result[0].isin, "123456789")
    }
}
