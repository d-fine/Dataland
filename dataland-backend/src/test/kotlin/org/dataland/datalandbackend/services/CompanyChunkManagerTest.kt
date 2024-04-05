package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.BasicCompanyInformation
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class CompanyChunkManagerTest {
    lateinit var companyChunkManager: CompanyChunkManager
    lateinit var mockCompanyQueryManager: CompanyQueryManager

    private val totalNumberOfCompanies = 100
    private var testCompanyList = mutableListOf<BasicCompanyInformation>()
    private val emptyFilter = StoredCompanySearchFilter(
        emptyList(),
        emptyList(),
        emptyList(),
        "",
    )

    @BeforeEach
    fun initilizeCompanyChunkManager() {
        for (i in 0..<totalNumberOfCompanies) {
            this.testCompanyList.addLast(
                object : BasicCompanyInformation {
                    override val companyId: String = "Test CompanyId"
                    override val companyName: String = "Test Company $i"
                    override val headquarters: String = "Test Headquarters"
                    override val countryCode: String = "Test CountryCode"
                    override val sector: String? = null
                    override val lei: String? = null
                },
            )
        }
        this.testCompanyList.sortedBy { it.companyName }
        mockCompanyQueryManager = Mockito.mock(CompanyQueryManager::class.java)
        Mockito.`when`(mockCompanyQueryManager.searchCompaniesAndGetApiModel(emptyFilter)).thenReturn(testCompanyList)

        companyChunkManager = CompanyChunkManager(mockCompanyQueryManager)
    }

    @Test
    fun `check that the number of companies is correct`() {
        Mockito.`when`(mockCompanyQueryManager.searchCompaniesAndGetApiModel(emptyFilter)).thenReturn(testCompanyList)
        val numberOfCompanies = companyChunkManager.returnNumberOfCompanies(emptyFilter)
        Assertions.assertEquals(totalNumberOfCompanies, numberOfCompanies)
    }

    @Test
    fun `check that all companies are retrieved if no chunkSize is specified`() {
        Mockito.`when`(mockCompanyQueryManager.searchCompaniesAndGetApiModel(emptyFilter)).thenReturn(testCompanyList)
        val companies = companyChunkManager.returnCompaniesInChunks(null, 0, emptyFilter)
        Assertions.assertEquals(totalNumberOfCompanies, companies.size)
        for (i in 0..<companies.size) {
            Assertions.assertEquals(testCompanyList[i].companyName, companies[i].companyName)
        }
    }

    @Test
    fun `check that different chunks are retrieved`() {
        val emptyFilter = StoredCompanySearchFilter(
            emptyList(),
            emptyList(),
            emptyList(),
            "",
        )
        val chunkSize = 10
        val companies = companyChunkManager.returnCompaniesInChunks(chunkSize, 1, emptyFilter)
        Assertions.assertEquals(chunkSize, companies.size)
        for (i in 10..<companies.size) {
            Assertions.assertEquals(testCompanyList[i].companyName, companies[i].companyName)
        }
    }
}
