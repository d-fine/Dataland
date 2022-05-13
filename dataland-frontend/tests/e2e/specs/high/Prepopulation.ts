describe('Population Test',  () => {
    Cypress.config({
        defaultCommandTimeout: 480000
    })

    let companiesWithData: any

    before(function () {
        cy.fixture('CompanyInformationWithEuTaxonomyData').then(function (companies) {
            companiesWithData = companies
        });
    });

    async function uploadCompanyWithData(companiesWithData: Array<object>) {
        console.time(`The elapsed time to upload ${companiesWithData.length} companies with data`)
        const chunkSize = 50;
        for (let i = 0; i < companiesWithData.length; i += chunkSize) {
            const chunk = companiesWithData.slice(i, i + chunkSize);
            await Promise.all(chunk.map(async (element: any) => {
                    await fetch(`${Cypress.env("API")}/companies`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(element["companyInformation"])
                    }).then( async (response) => {
                        assert(response.status.toString() === "200",
                            `Got status code ${response.status.toString()} for companyInformation index ${i}. Expected: 200`)
                        const euTaxonomyData = element["euTaxonomyData"]
                        const data  = await response.json()
                        euTaxonomyData["companyId"] = data.companyId
                        console.log(euTaxonomyData)
                        await fetch(`${Cypress.env("API")}/data/eutaxonomies`, {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify(euTaxonomyData)
                        }).then(response => {
                            assert(response.status.toString() === "200",
                                `Got status code ${response.status.toString()} for euTaxonomyData index ${i}. Expected: 200`)
                        })
                    })
                })
            )
        }
        console.timeEnd(`The elapsed time to upload ${companiesWithData.length} companies with data`)
    }


    it.only('Populate Companies with Data', async () => {
        await uploadCompanyWithData(companiesWithData)
    });

    it('Check if all the company ids can be retrieved', () => {
        cy.retrieveCompanyIdsList().then((companyIdList: Array<any>) => {
            assert(companyIdList.length >= companiesWithData.length, // >= to avoid problem with several runs in a row
                `Uploaded ${companyIdList.length} out of ${companiesWithData.length} companies`)
            assert(companyIdList.every(companyId => typeof companyId === "string"), "Validation of company Ids")
        })
    });

    it('Check if all the data ids can be retrieved', () => {
        cy.retrieveDataIdsList().then((dataIdList: Array<any>) => {
            assert(dataIdList.length >= companiesWithData.length, // >= to avoid problem with several runs in a row
                `Uploaded ${dataIdList.length} out of ${companiesWithData.length} data`)
            assert(dataIdList.every(dataId => typeof dataId === "string"), "Validation of data Ids")
        })
    });

    it('Check Data Presence and Link route for EU Taxonomy Data', () => {
        cy.retrieveDataIdsList().then((dataIdList: Array<string>) => {
            cy.visit("/data/eutaxonomies/" + dataIdList[0])
            cy.get('h3', { timeout: 60000 }).should('be.visible')
            cy.get('h3').contains("Revenue")
            cy.get('h3').contains("CapEx")
            cy.get('h3').contains("OpEx")
            cy.get('.d-card').should('contain', 'Eligible')
            cy.get('.d-card .p-progressbar').should('exist')
        });
    })

    it('Check Data Presence and Link route for companies with eutaxonomies data', () => {
        cy.retrieveCompanyIdsList().then((companyIdList: Array<string>) => {
            cy.visit(`/companies/${companyIdList[0]}/eutaxonomies`)
            cy.get('h3', { timeout: 60000 }).should('be.visible')
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


    it.skip('Company Name Input field exists and works after population', () => {
        const inputValue = companiesWithData[0].companyInformation.companyName
        cy.visit("/search")
        cy.get('input[name=companyName]')
            .should('not.be.disabled')
            .type(inputValue, {force: true})
            .should('have.value', inputValue)
        cy.intercept('**/api/companies*').as('retrieveCompany')
        cy.get('button[name=getCompanies]').click()
        cy.wait('@retrieveCompany', {timeout: 60000}).then(() => {
            cy.get('td').contains("VIEW")
                .contains('a', 'VIEW')
                .click().url().should('include', '/companies/')
        })
    });

    it('Show all companies button exists after population', () => {
        cy.visit("/search")
        cy.get('button.p-button').contains('Show all companies')
            .should('not.be.disabled')
            .click()
    });
});