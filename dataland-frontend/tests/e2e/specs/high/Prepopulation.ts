describe('Population Test', () => {
    Cypress.config({
        defaultCommandTimeout: 0
    })

    let eutaxonomiesData: any
    let companiesData: any
    const companyAssociatedEuTaxonomyData: any = []

    before(function () {
        cy.fixture('EuTaxonomyData').then(function (eutaxonomies) {
            eutaxonomiesData = eutaxonomies
        });
        cy.fixture('CompanyInformation').then(function (companies) {
            companiesData = companies
        });
    });

    async function uploadData(dataArray: Array<object>, endpoint: string) {
        console.time(`The elapsed time to upload ${dataArray.length} items for '${endpoint}'`)
        const chunkSize = 80;
        for (let i = 0; i < dataArray.length; i += chunkSize) {
            const chunk = dataArray.slice(i, i + chunkSize);
            await Promise.all(chunk.map(async (element: object) => {
                    await fetch(`${Cypress.env("API")}/${endpoint}`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(element)
                    }).then(response => {
                        assert(response.status.toString() === "200",
                            `Got status code ${response.status.toString()} for index ${i}. Expected: 200`)
                    })
                })
            )
        }
        console.timeEnd(`The elapsed time to upload ${dataArray.length} items for '${endpoint}'`)
    }


    it('Populate Companies', async () => {
        await uploadData(companiesData, "companies")
    });

    it('Check if all the company ids can be retrieved', () => {
        cy.retrieveCompanyIdsList().then((companyIdList: any) => {
            assert(companyIdList.length >= companiesData.length, // >= to avoid problem with several runs in a row
                `Uploaded ${companyIdList.length} out of ${companiesData.length} companies`)
            for (const companyIdIndex in companyIdList) {
                const companyId = companyIdList[companyIdIndex]
                assert(typeof companyId !== 'undefined',
                    `Validation of company number ${companyIdIndex}`)
                if (typeof eutaxonomiesData[companyIdIndex] == "object") {
                    companyAssociatedEuTaxonomyData.push({"companyId": companyId, "data": eutaxonomiesData[companyIdIndex]})
                }
            }
        })
    });

    it('Populate EU Taxonomy Data', async () => {
        await uploadData(companyAssociatedEuTaxonomyData, "data/eutaxonomies")
    });

    it('Check if all the data ids can be retrieved', () => {
        cy.retrieveDataIdsList().then((dataIdList: any) => {
            assert(dataIdList.length >= eutaxonomiesData.length, // >= to avoid problem with several runs in a row
                `Uploaded ${dataIdList.length} out of ${eutaxonomiesData.length} data`)
            for (const dataIdIndex in dataIdList) {
                assert(typeof dataIdList[dataIdIndex] !== 'undefined',
                    `Validation of data number ${dataIdIndex}`)
            }
        })
    });

});

describe('EU Taxonomy Data', () => {
    it('Check Data Presence and Link route', () => {
        cy.retrieveDataIdsList().then((dataIdList: any) => {
            cy.visit("/data/eutaxonomies/" + dataIdList[0])
            cy.get('h3', { timeout: 2000 }).should('be.visible')
            cy.get('h3').contains("Revenue")
            cy.get('h3').contains("CapEx")
            cy.get('h3').contains("OpEx")
            cy.get('.d-card').should('contain', 'Eligible')
            cy.get('.d-card .p-progressbar').should('exist')
        });
    })
});

describe('Company EU Taxonomy Data', () => {
    it('Check Data Presence and Link route', () => {
        cy.retrieveCompanyIdsList().then((companyIdList: any) => {
            cy.visit(`/companies/${companyIdList[0]}/eutaxonomies`)
            cy.get('h3', { timeout: 2000 }).should('be.visible')
            cy.get('h3').contains("Revenue")
            cy.get('h3').contains("CapEx")
            cy.get('h3').contains("OpEx")
            cy.get('body').contains("Market Cap:")
            cy.get('body').contains("Headquarter:")
            cy.get('body').contains("Sector:")
            cy.get('.grid.align-items-end.text-left').contains('Financial Data 2021')
            cy.get('.grid.align-items-end.text-left').contains('Sustainability Data 2021')
            cy.get('input[name=eu_taxonomy_search_input]').should('exist')
        })
    });
});

describe('Company Data', () => {
    let companiesData: any
    before(function () {
        cy.fixture('CompanyInformation').then(function (companies) {
            companiesData = companies
        });

    });
    it('Company Name Input field exists and works', () => {
        const inputValue = companiesData[0].companyName
        cy.visit("/search")
        cy.get('input[name=companyName]')
            .should('not.be.disabled')
            .type(inputValue, {force: true})
            .should('have.value', inputValue)
        cy.get('button[name=getCompanies]').click()
        cy.get('td').contains("VIEW")
            .contains('a', 'VIEW')
            .click().url().should('include', '/companies/')
    });

    it('Show all companies button exists', () => {
        cy.visit("/search")
        cy.get('button.p-button').contains('Show all companies')
            .should('not.be.disabled')
            .click()
    });
});