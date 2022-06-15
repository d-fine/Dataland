describe('Data Upload Page', function () {
    beforeEach(()=> {
        cy.restoreLoginSession()
    })
    it('page should be present', function () {
        cy.visit("/upload")
        cy.get('#app').should("exist")
    });
    it('Create a company is present', () => {
        cy.get('.p-card-title').should("contain", "Create a Company")
    });
    it('Company name Input field exists and works', () => {
        const inputValue = "A company name"
        cy.get('input[name=companyName]')
            .should('not.be.disabled')
            .click({force: true})
            .type(inputValue)
            .should('have.value', inputValue)
    });
    it('Post company button is present', () => {
        cy.get('button[name="postCompanyData"]').contains('Post Company')
            .should('not.be.disabled')
    });
});