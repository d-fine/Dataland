package org.dataland.documentmanager.entities

import org.dataland.datalandbackendutils.model.DocumentCategory
import org.dataland.datalandbackendutils.model.DocumentType
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.stream.Stream

class DocumentMetaInfoEntityTest {
    private val mockSecurityContext = mock<SecurityContext>()
    private lateinit var documentMetaInfoEntity: DocumentMetaInfoEntity

    companion object {
        private const val DUMMY_USER_ID = "dummy-user-id"

        fun <T> cartesianProduct(lists: List<List<T>>): List<List<T>> =
            lists.fold(listOf(listOf())) { acc, list ->
                acc.flatMap { accList ->
                    list.map { accList + it }
                }
            }

        @JvmStatic
        fun provideParametersForTestingIsViewableByUser(): Stream<Arguments> {
            val possibleQaStatus = listOf(QaStatus.Accepted, QaStatus.Pending)
            val possibleUploaderIds = listOf(DUMMY_USER_ID, "dummy-uploader-id")
            val possibleIsUserLoggedInValues = listOf(true, false)
            val possibleUserRoleSets =
                listOf(
                    setOf<DatalandRealmRole>(),
                    setOf(DatalandRealmRole.ROLE_ADMIN),
                    setOf(DatalandRealmRole.ROLE_REVIEWER),
                    setOf(DatalandRealmRole.ROLE_ADMIN, DatalandRealmRole.ROLE_REVIEWER),
                )

            val cartesianProductOfPossibilities =
                cartesianProduct(
                    listOf(
                        possibleQaStatus, possibleUploaderIds, possibleIsUserLoggedInValues, possibleUserRoleSets,
                    ),
                )

            val argumentsList = mutableListOf<Arguments>()

            cartesianProductOfPossibilities.forEach {
                argumentsList.add(
                    Arguments.of(
                        it[0], it[1], it[2], it[3],
                    ),
                )
            }

            return argumentsList.stream()
        }
    }

    @BeforeEach
    fun setUp() {
        reset(mockSecurityContext)
    }

    @ParameterizedTest
    @MethodSource("provideParametersForTestingIsViewableByUser")
    fun `check that the method isViewableByUser shows the correct output behavior`(
        qaStatus: QaStatus,
        uploaderId: String,
        isUserLoggedIn: Boolean,
        userRoles: Set<DatalandRealmRole>,
    ) {
        val mockAuthentication =
            if (isUserLoggedIn) {
                AuthenticationMock.mockJwtAuthentication(
                    username = "dummy-user",
                    userId = DUMMY_USER_ID,
                    roles = userRoles,
                )
            } else {
                mock<DatalandJwtAuthentication>()
            }

        doReturn(mockAuthentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)

        documentMetaInfoEntity =
            DocumentMetaInfoEntity(
                documentId = "dummy-document-id",
                documentType = DocumentType.Pdf,
                documentName = "dummy-document",
                documentCategory = DocumentCategory.AnnualReport,
                companyIds = mutableSetOf("dummy-company-id"),
                uploaderId = uploaderId,
                uploadTime = 0L,
                publicationDate = null,
                reportingPeriod = null,
                qaStatus = qaStatus,
            )

        when {
            qaStatus == QaStatus.Accepted ->
                assertTrue(
                    documentMetaInfoEntity.isViewableByUser(),
                    "Method returned false although QaStatus is Accepted.",
                )
            !isUserLoggedIn ->
                assertFalse(
                    documentMetaInfoEntity.isViewableByUser(),
                    "Method returned true although QaStatus is Pending and user is not logged in.",
                )
            uploaderId != DUMMY_USER_ID && userRoles.isEmpty() ->
                assertFalse(
                    documentMetaInfoEntity.isViewableByUser(),
                    "Method returned true although all conditions failed.",
                )
            else ->
                assertTrue(
                    documentMetaInfoEntity.isViewableByUser(),
                    "Method returned false although user should have the right to view the non-accepted document.",
                )
        }
    }
}
