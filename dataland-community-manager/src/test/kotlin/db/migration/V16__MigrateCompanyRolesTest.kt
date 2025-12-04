package db.migration

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.dataland.datalandcommunitymanager.repositories.CompanyRoleAssignmentRepository
import org.dataland.datalandbackendutils.services.utils.BaseFlywayMigrationTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

@SpringBootTest(
    classes = [org.dataland.datalandcommunitymanager.DatalandCommunityManager::class],
    properties = ["spring.jpa.hibernate.ddl-auto=none"]
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Suppress("ClassName")
class V16__MigrateCompanyRolesTest : BaseFlywayMigrationTest() {

    @Autowired
    lateinit var companyRoleAssignmentRepository: CompanyRoleAssignmentRepository

    @Autowired
    @PersistenceContext
    lateinit var entityManager: EntityManager

    @Autowired
    lateinit var transactionManager: PlatformTransactionManager

    private val expectedRenaming = mapOf(
        "MemberAdmin" to "Admin",
        "Member" to "Analyst"
    )

    private val testAssignments = mapOf(
        UUID.randomUUID().toString() to Triple(UUID.randomUUID().toString(), "MemberAdmin", "Member"),
        UUID.randomUUID().toString() to Triple(UUID.randomUUID().toString(), "Member", "Analyst")
    )

    override fun getFlywayBaselineVersion(): String = "15"
    override fun getFlywayTargetVersion(): String = "16"

    override fun setupBeforeMigration() {
        val txTemplate = TransactionTemplate(transactionManager)
        txTemplate.execute {
            testAssignments.map {
                """
                INSERT INTO company_role_assignments (company_id, user_id, company_role)
                VALUES ('${it.key}', '${it.value.first}', '${it.value.second}')
            """.trimIndent()
            }.forEach { entityManager.createNativeQuery(it).executeUpdate() }
        }
    }

    @Test
    fun `verify migration script renames company roles correctly`() {
        val migratedAssignments = companyRoleAssignmentRepository.findAll()
            .filter { it.userId in testAssignments.keys }

        Assertions.assertEquals(testAssignments.size, migratedAssignments.size)

        migratedAssignments.forEach { newAssignment ->
            val oldRole = testAssignments[newAssignment.userId]?.second
            val expectedRole = expectedRenaming[oldRole] ?: oldRole
            Assertions.assertEquals(expectedRole, newAssignment.companyRole.toString())
        }
    }
}
