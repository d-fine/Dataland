describe('Population Test', () => {
    let eutaxonomiesData:any
    let companiesData:any
    before(function(){
        cy.fixture('eutaxonomies').then(function(eutaxonomies){
            eutaxonomiesData=eutaxonomies
        });
        cy.fixture('companies').then(function(companies){
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
});

describe('EU Taxonomy Data', () => {
    it('Check Data Presence and Link route', () => {
        cy.visit("/data/eutaxonomies/1")
        cy.get('h1').contains("Company Data")
        cy.get('h4').contains("EU Taxonomy Data")
    });
});

describe('Company Data', () => {
    let companiesData:any
    before(function(){
        cy.fixture('companies').then(function(companies){
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
        cy.get('table').contains('Company Search')
        cy.get('td').contains("d-fine").click().url().should('include', '/companies/')
    });

    it('Show all companies button exists', () => {
        cy.visit("/search")
        cy.get('button.btn').contains('Show all companies')
            .should('not.be.disabled')
            .click()
        cy.get('table').contains('Company Search')
    });
});