package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.repositories.DataMetaInformationRepository
import org.dataland.datalandbackend.repositories.DataPointMetaInformationRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackend.utils.DataBaseCreationUtils
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.datalandbackend.utils.TestResourceFileReader
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.infrastructure.ClientException
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import org.dataland.specificationservice.openApiClient.model.IdWithRef
import org.dataland.specificationservice.openApiClient.model.SimpleFrameworkSpecification
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.dataland.datalandbackend.utils.DEFAULT_COMPANY_ID as companyId
import org.dataland.datalandbackend.utils.DEFAULT_DATA_POINT_TYPE as dataPointType
import org.dataland.datalandbackend.utils.DEFAULT_FRAMEWORK as framework
import org.dataland.datalandbackend.utils.DEFAULT_REPORTING_PERIOD as reportingPeriod

/**
 * Shared setup for [DataAvailabilityChecker] tests, split across
 * [DataAvailabilityCheckerFilterTest] and [DataAvailabilityCheckerSearchTest].
 */
@SpringBootTest(
    classes = [DatalandBackend::class],
    properties = ["spring.rabbitmq.listener.simple.auto-startup=false"],
)
@DefaultMocks
abstract class DataAvailabilityCheckerTestBase : BaseIntegrationTest() {
    companion object {
        const val EXACTLY_ONE_RESULT_MESSAGE = "There should be exactly one result."
        const val BOTH_DIMENSIONS_SHOULD_BE_IN_RESULT_MESSAGE = "Both dimensions should be returned."
    }

    @Autowired
    private lateinit var dataMetaInformationRepository: DataMetaInformationRepository

    @Autowired
    private lateinit var dataPointMetaInformationRepository: DataPointMetaInformationRepository

    @Autowired
    private lateinit var storedCompanyRepository: StoredCompanyRepository

    @Autowired
    protected lateinit var dataAvailabilityChecker: DataAvailabilityChecker

    @Autowired
    protected lateinit var specificationClient: SpecificationControllerApi

    @Autowired
    private lateinit var specificationService: SpecificationService

    @Autowired
    private lateinit var dataCompositionService: DataCompositionService

    protected lateinit var dbCreationUtils: DataBaseCreationUtils

    protected val datasetDimension =
        BasicDataDimensions(companyId = companyId, dataType = framework, reportingPeriod = reportingPeriod)
    protected val dataPointDimension =
        BasicDataDimensions(companyId = companyId, dataType = dataPointType, reportingPeriod = reportingPeriod)

    private val inputFrameworkSpecification = "./json/frameworkTemplate/frameworkSpecification.json"
    private val frameworkSpecification = TestResourceFileReader.getKotlinObject<FrameworkSpecification>(inputFrameworkSpecification)

    /**
     * Builds a minimal, valid [DataPointTypeSpecification] for the given data point type so that mocked calls to
     * the specification client do not return null (which would cause an NPE further down the line, since specs are
     * cached in a [java.util.concurrent.ConcurrentHashMap] that does not allow null values).
     */
    private fun makeDataPointTypeSpecification(dataPointType: String) =
        DataPointTypeSpecification(
            dataPointType = IdWithRef(id = dataPointType, ref = ""),
            name = dataPointType,
            businessDefinition = "",
            dataPointBaseType = IdWithRef(id = "numeric", ref = ""),
            usedBy = listOf(IdWithRef(id = framework, ref = "")),
            calculationRules = emptyList(),
        )

    @BeforeEach
    fun setUp() {
        whenever(specificationClient.listFrameworkSpecifications()).thenReturn(
            listOf(SimpleFrameworkSpecification(IdWithRef(framework, "dummy"), "Test Framework")),
        )
        whenever(specificationClient.getFrameworkSpecification(framework)).thenReturn(frameworkSpecification)

        doThrow(ClientException()).whenever(specificationClient).getDataPointTypeSpecification(framework)
        dbCreationUtils =
            DataBaseCreationUtils(
                storedCompanyRepository = storedCompanyRepository,
                dataMetaInformationRepository = dataMetaInformationRepository,
                dataPointMetaInformationRepository = dataPointMetaInformationRepository,
            )

        specificationService.initiateSpecifications(null)

        dataCompositionService.getRelevantDataPointTypes(framework).forEach { relevantDataPointType ->
            whenever(specificationClient.getDataPointTypeSpecification(relevantDataPointType))
                .thenReturn(makeDataPointTypeSpecification(relevantDataPointType))
        }
    }
}
