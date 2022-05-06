describe('Tooltips test suite', () => {
    let dataIdList:any
    it('Retrieve data ID list', () => {
        cy.request('GET', `${Cypress.env("API")}/metadata`).then((response) => {
            dataIdList = response.body.map(function (e: any) {
                return e.dataId
            })
        })
    });

    it('tooltips are present and contain text as expected', function () {
        const NFRDText = "Non financial disclosure directive"
        const AssuranceText = "Level of Assurance specifies the confidence level"
        cy.visit("/companies/"+dataIdList[8]+"/eutaxonomies")
        cy.get('#app').should("exist")
        cy.get('.p-card-content .col-12.text-left strong')
            .contains('NFRD required')
            .trigger('mouseenter', "center")
        cy.get('.p-tooltip')
            .should('be.visible')
            .contains(NFRDText)
        cy.get('.p-card-content .col-12.text-left strong')
            .contains('NFRD required')
            .trigger('mouseleave' )
        cy.get('.p-tooltip')
            .should('not.exist')
        cy.get('.p-card-content .col-12.text-left strong')
            .contains('Level of Assurance')
            .trigger('mouseenter', "center")
        cy.get('.p-tooltip')
            .should('be.visible')
            .contains(AssuranceText)
    });
})


