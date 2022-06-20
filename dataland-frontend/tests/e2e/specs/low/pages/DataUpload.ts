describe('Data Upload Page', function () {
    beforeEach(()=> {
        cy.restoreLoginSession()
    })
    it('page should be present', function () {
        cy.visit("/upload")
        cy.get('#app').should("exist")
        cy.get('.p-card-title').should("contain", "Create a Company")
        const inputValue = "A company name"
        cy.get('input[name=companyName]')
            .should('not.be.disabled')
            .click({force: true})
            .type(inputValue)
            .should('have.value', inputValue)
        cy.get('button[name="postCompanyData"]').contains('Post Company')
            .should('not.be.disabled')
    });
});