let idList: any
describe('Population Test', () => {
    let eutaxonomiesData:any
    let companiesData:any
    before(function(){
        cy.fixture('CompanyAssociatedEuTaxonomyData').then(function(eutaxonomies){
            eutaxonomiesData=eutaxonomies
        });
        cy.fixture('CompanyInformation').then(function(companies){
            companiesData=companies
        });

    });


    it('Populate Companies', function (){
        for (const index in companiesData) {
            cy.request('POST', `${Cypress.env("API")}/companies`, companiesData[index]).its('status').should("equal", 200)

        }
        console.log(companiesData)
    });

    it('Populate EU Taxonomy Data', function (){
        for (const index in eutaxonomiesData) {
            cy.request('POST', `${Cypress.env("API")}/data/eutaxonomies`, eutaxonomiesData[index]).its('status').should("equal", 200)
        }
        console.log(eutaxonomiesData)
    });

    it('Retrieve data ID list', () => {
        cy.request('GET', `${Cypress.env("API")}/metadata`).then((response) => {
            idList = response.body.map(function (e:string){
                return parseInt(Object.values(e)[0])
            })
        })
    });


});

describe('EU Taxonomy Data', () => {
    it('Check Data Presence and Link route', () => {
        cy.visit("/data/eutaxonomies/"+idList[0])
        cy.get('h3').contains("Revenue")
        cy.get('h3').contains("CapEx")
        cy.get('h3').contains("OpEx")
        cy.get('.d-card').should('contain', 'Eligible')
        cy.get('.d-card .p-progressbar').should('exist')
    });
});

describe('Company EU Taxonomy Data', () => {
    it('Check Data Presence and Link route', () => {
        cy.visit("/companies/1/eutaxonomies")
        cy.get('h3').contains("Revenue")
        cy.get('h3').contains("CapEx")
        cy.get('h3').contains("OpEx")
        cy.get('body').contains("Market Cap:")
        cy.get('body').contains("Headquarter:")
        cy.get('body').contains("Sector:")
        cy.get('button.p-button.p-component').contains('Financial and sustainability')
        cy.get('input[name=eu_taxonomy_search_input]').should('exist')
    });

});

describe('Company Data', () => {
    let companiesData:any
    before(function(){
        cy.fixture('CompanyInformation').then(function(companies){
            companiesData=companies
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