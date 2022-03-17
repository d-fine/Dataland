import { mount } from 'cypress'
import WelcomeDataland from '@/components/WelcomeDataland'

describe('WelcomeDataland testsuite', () => {
    it('Logo and welcome message are present', () => {
        mount(WelcomeDataland)
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

})


