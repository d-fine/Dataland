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
describe('Sample Section', () => {
    it('Check if App is present', () => {
        cy.visit("/")
        cy.get('#app').should('exist')
    })
    it('Logo and welcome message are present', () => {
        cy.get('h2').should("contain.text","Explore Dataland")
        cy.get('button[name="eu_taxonomy_sample_button"]')
            .should('be.visible')
            .should('contain','EU TAXONOMY')
            .click({force:true})
            .url().should('include', '/companies/')
            .url().should('include', '/eutaxonomies')
        cy.get('h2').contains('EU Taxonomy Data')

    })

})


