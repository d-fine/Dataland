
describe('EU Taxo Data testsuite', () => {
    it('Check if App is present', () => {
        cy.visit("/eutaxonomies")
        cy.get('#app').should('exist')
    });
    it('Table should be present', () => {
        cy.get('table h4').should("have.text","Available datasets")
    });
    it('Table headings present', () => {
        cy.get('table th').contains("Data ID")
        cy.get('table th').contains("Data Type")
        cy.get('table th').contains("Link")
    });

})




