describe('Index testsuite', () => {
    it('Check App is present', () => {
        cy.visit("/")
        cy.get('#app')
    })
    it('Logo and welcome message are present', () => {
        cy.get('h1').should("have.text","Welcome to DataLand")
        cy.get('img[alt="Dataland logo"]')
            .should('be.visible')
            .should('have.attr', 'src')
            .should('include','dataland-logo')

        cy.get('img[alt="Dataland logo"]')
            .should('be.visible')
            .and(($img) => {
                expect($img[0].naturalWidth).to.be.greaterThan(0)})
    })
    it('Contact Data Form is present', () => {
        cy.get('.card-title h2').should("have.text", "Contact Data Search")
    })
    it('Country Code Input field exists and works', () => {
        const inputValue = "A 3 letter country code"
        cy.get('#countryCode')
            .should('not.be.disabled')
            .click({force:true})
            .type(inputValue)
            .should('have.value', inputValue)
    })
    it('Company name Input field exists and works', () => {
        const inputValue = "A company name"
        cy.get('#companyName')
            .should('not.be.disabled')
            .click({force:true})
            .type(inputValue)
            .should('have.value', inputValue)
        })
    it('Clear button exists and clears all', () => {
        cy.get('button.btn.btn-sm').contains('Clear')
            .should('not.be.disabled')
            .click()
        cy.get('#countryCode').should("have.value",'')
        cy.get('#companyName').should("have.value",'')
        })
    it('Skyminder button is present', () => {
        expect(true).equal(true)
        cy.get('button.btn.btn-sm').contains('Get Skyminder by Name')
            .should('not.be.disabled')
    })
})


