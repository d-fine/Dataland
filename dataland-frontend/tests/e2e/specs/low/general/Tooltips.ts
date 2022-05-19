describe('Tooltips test suite', () => {
    it('tooltips are present and contain text as expected', function () {
        const NFRDText = "Non financial disclosure directive"
        const AssuranceText = "Level of Assurance specifies the confidence level"
        cy.intercept('**/api/companies/*').as('retrieveCompany')
        cy.retrieveCompanyIdsList().then((companyIdList: any) => {
            cy.visit("/companies/" + companyIdList[0] + "/eutaxonomies")
            cy.wait('@retrieveCompany', {timeout: 2000}).then(() => {
                cy.get('#app', {timeout: 2000}).should("exist")
                cy.get('.p-card-content .col-12.text-left strong')
                    .contains('NFRD required')
                    .trigger('mouseenter', "center")
                cy.get('.p-tooltip')
                    .should('be.visible')
                    .contains(NFRDText)
                cy.get('.p-card-content .col-12.text-left strong')
                    .contains('NFRD required')
                    .trigger('mouseleave')
                cy.get('.p-tooltip')
                    .should('not.exist')
                cy.get('.p-card-content .col-12.text-left strong')
                    .contains('Level of Assurance')
                    .trigger('mouseenter', "center")
                cy.get('.p-tooltip')
                    .should('be.visible')
                    .contains(AssuranceText)
            })
        });
    });
})


