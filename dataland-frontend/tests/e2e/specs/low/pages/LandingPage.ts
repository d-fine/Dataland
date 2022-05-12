describe('Login Section', () => {
    it('Check if App is present', () => {
        cy.visit("/")
        cy.get('#app').should('exist')
    })
    it('Logo and welcome message are present', () => {
        cy.get('h1').should("contain.text","CREATE A DATASET")
        cy.get('img[alt="Dataland logo"]')
            .should('be.visible')
            .should('have.attr', 'src')
            .should('include','vision')
    })

})
describe.only('Sample Section', () => {
    it('Check that the sample section works properly', () => {
        cy.visit("/")
        cy.get('h2').should("contain.text","Explore Dataland")
        cy.get('button[name=eu_taxonomy_sample_button]')
            .should('be.visible')
            .should("contain.text","EU Taxonomy")
            .click({force:true})
            .url().should('include', '/taxonomysample')
        cy.get('h2').contains('EU Taxonomy Data')
        cy.get('.p-button.p-button-rounded')
            .should("contain.text","COMPANY DATA SAMPLE")
        cy.get('body').should('contain.text', 'Join Dataland with others')
        cy.get('[title=back_button').should('be.visible').click({force:true})
        cy.get('h1').should("contain.text","CREATE A DATASET")

    })

})


