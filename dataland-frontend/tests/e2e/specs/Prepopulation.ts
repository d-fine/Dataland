
describe('Population Test', function () {
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
            cy.request('POST', `${Cypress.env("API")}/eutaxonomies`, eutaxonomiesData[index]).its('status').should("equal", 200)
        }
        console.log(eutaxonomiesData)
    });
});

describe('EU Taxonomy Data', () => {
    it('Check Data Presence and Link route', () => {
        cy.visit("/companies/1")
        cy.get('td').contains("1")
        cy.get('td').contains("EuTaxonomyData").click().url().should('include', '/eutaxonomies/1')
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
            .click({force: true})
            .type(inputValue)
            .should('have.value', inputValue)
        cy.get('button[name=getCompanies]').click()
        cy.get('table').contains('Company Search')
        cy.get('td').contains("d-fine").click().url().should('include', '/companies/')
    });

    it('Show all companies button exists', () => {
        cy.visit("/search")
        cy.get('button.p-button').contains('Show all companies')
            .should('not.be.disabled')
            .click()
        cy.get('table').contains('Company Search')
    });
});
