describe('Back Button test suite', () => {
    it('company eu taxonomy page should be present and contain back button', function () {
        cy.restoreLoginSession()
        cy.visit("/searchtaxonomy")
        cy.retrieveDataIdsList().then((dataIdList: any) => {
            cy.visit("/companies/" + dataIdList[5] + "/eutaxonomies")
            cy.get('#app').should("exist")
            cy.get('span.text-primary[title=back_button]')
                .parent('.cursor-pointer.grid.align-items-center')
                .click()
                .url().should('include', '/searchtaxonomy')
        });
    });
})


