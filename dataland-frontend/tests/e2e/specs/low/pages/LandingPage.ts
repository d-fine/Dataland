describe('Login Section', () => {
    it('Check if App is present', () => {
        cy.visit("/")
        cy.get('#app').should('exist')
        cy.get('h1').should("contain.text","CREATE A DATASET")
        cy.get('img[alt="Dataland logo"]')
            .should('be.visible')
            .should('have.attr', 'src')
            .should('include','vision')
        cy.get('button[name=get_started_button]')
            .should('be.visible')
            .should("contain.text","Get Started")
    })
    it('Company logos are present', () => {
        cy.visit("/")
        cy.get('img[alt="pwc"]')
            .should('be.visible')
            .should('have.attr', 'src')
        cy.get('img[alt="d-fine GmbH"]')
            .should('be.visible')
            .should('have.attr', 'src')
    })
})

describe('Marketing Section', () => {
    it('Checks that the marketing section works properly', () => {
        cy.visit("/")
        cy.get("h2").should("contain.text", "Learn about our vision")
        cy.get('img[alt="Flow Diagramm"]')
            .should('be.visible')
            .should('have.attr', 'src')
        cy.get('h3').contains('Bring together who')
        cy.get('img[alt="Data Workflow"]')
            .should('be.visible')
            .should('have.attr', 'src')
        cy.get('h3').contains('Maximize data coverage')
    })

})

describe('Footer Section', () => {
    it('Checks that the footer section works properly', () => {
        cy.visit("/")
        cy.get('img[alt="Dataland logo"]')
            .should('be.visible')
            .should('have.attr', 'src')
            .should('include','vision')
        cy.get('body').should("contain.text", "Legal")
        cy.get('body').should("contain.text", "Copyright © 2022 Dataland")
        cy.get('a span[title="imprint"]')
            .should("contain.text", "Imprint")
            .click({force:true})
            .url().should('include', '/imprint')
        cy.get("h2").contains('Imprint')
        cy.get('[title=back_button').click({force:true})
        cy.get('a p[title="data privacy"]')
            .should("contain.text", "Data Privacy")
            .click({force:true})
            .url().should('include', '/dataprivacy')
        cy.get("h2").contains('Data Privacy')
    })
})

describe('Sample Section', () => {
    function visitSamplePage() {
        cy.visit("/")
        cy.get('h2').should("contain.text", "Explore Dataland")
        cy.get('button[name=eu_taxonomy_sample_button]')
            .should('be.visible')
            .should("contain.text", "EU Taxonomy")
            .click({force: true})
            .url().should('include', '/taxonomysample')
        cy.get('h2').contains('EU Taxonomy Data')
        cy.get('.p-button.p-button-rounded')
            .should("contain.text", "COMPANY DATA SAMPLE")
        cy.get('body').should('contain.text', 'Join Dataland with other')
        cy.get('[title=back_button').should('be.visible').click({force: true})
        cy.url().should("eq", Cypress.config('baseUrl') + "/")
    }

    it('Check that the sample section works properly with authentication', () => {
        cy.restoreLoginSession()
        visitSamplePage();
    })
    it('Check that the sample section works properly without authentication', () => {
        visitSamplePage();
    })
})
