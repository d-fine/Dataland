import {login} from "../../../support/utility";

describe('Data Search Page Skyminder', function () {
    beforeEach(()=> {
        login()
    })
    it('page should be present', function () {
        cy.visit("/search")
        cy.get('#app').should("exist")
    });
    it('Contact Data Form is present', () => {
        cy.get('.p-card-title').should("contain", "Skyminder Data Search")
    });
    it('Country Code Input field exists and works', () => {
        const inputValue = "A 3 letter country code"
        cy.get('input[name=code]')
            .should('not.be.disabled')
            .click({force: true})
            .type(inputValue)
            .should('have.value', inputValue)
    });
    it('Company name Input field exists and works', () => {
        const inputValue = "A company name"
        cy.get('input[name=name]')
            .should('not.be.disabled')
            .click({force: true})
            .type(inputValue)
            .should('have.value', inputValue)
    });
    it('Clear button exists and clears all', () => {
        cy.get('button.p-button').contains('Clear')
            .should('not.be.disabled')
            .click()
        cy.get('input[name=code]').should("have.value", '')
        cy.get('input[name=name]').should("have.value", '')
    })
    it('Skyminder button is present', () => {
        cy.get('button[name="getSkyminderData"]').contains('Get Skyminder Data')
            .should('not.be.disabled')
    })
});

describe('Data Search Page Company', function () {
    beforeEach(()=> {
        login()
    })
    it('page should be present', function () {
        cy.visit("/search")
        cy.get('#app').should("exist")
    });
    it('Company Search form is present', () => {
        cy.get('.p-card-title').should("contain", "Company Search")
    });
    it('Company Name Input field exists and works', () => {
        const inputValue = "d-fine"
        cy.get('input[name=companyName]')
            .should('not.be.disabled')
            .click({force: true})
            .type(inputValue)
            .should('have.value', inputValue)
    });
    it('Should have button to search all companies', () => {
        cy.get('button[name=show_all_companies_button].p-button')
            .should('not.be.disabled')
            .should('contain', 'Show all companies')
            .click({force: true})
        cy.get('table.p-datatable-table').should('exist')
        cy.get('table.p-datatable-table').contains('th','COMPANY')
        cy.get('table.p-datatable-table').contains('th','SECTOR')
        cy.get('table.p-datatable-table').contains('th','MARKET CAP')
        cy.get('table.p-datatable-table').contains('td','VIEW')
            .contains('a', 'VIEW')
            .click()
            .url().should('include', '/companies/')
            .url().should('include', '/eutaxonomies')
    });

});