describe('Back Button test suite', () => {
    let dataIdList:any
    it('Retrieve data ID list', () => {
        cy.request('GET', `${Cypress.env("API")}/metadata`).then((response) => {
            dataIdList = response.body.map(function (e: any) {
                return e.dataId
            })
        })
    });
    it('company eu taxonomy page should be present and contain back button', function () {
        cy.visit("/searchtaxonomy")
        cy.visit("/companies/"+dataIdList[5]+"/eutaxonomies")
        cy.get('#app').should("exist")
        cy.get('span.text-primary[title=back_button]')
            .parent('.cursor-pointer.grid.align-items-center')
            .click()
            .url().should('include', '/searchtaxonomy')

    });
})


