describe('Tooltips test suite', () => {
    let idList:any
    it('Retrieve data ID list', () => {
        cy.request('GET', `${Cypress.env("API")}/metadata`).then((response) => {
            idList = response.body.map(function (e:string){
                return parseInt(Object.values(e)[2])
            })
        })
    });

    it('tooltips are present and contain text as expected', function () {
        const NFRDText = "Non financial disclosure directive"
        const AssuranceText = "Level of Assurance specifies the confidence level"
        cy.visit("/companies/"+idList[8]+"/eutaxonomies")
        cy.get('#app').should("exist")
        cy.get('.p-card-content .col-12.text-left strong')
            .contains('NFRD required')
            .trigger('mouseenter', "center")
        cy.get('.p-tooltip').contains(NFRDText)
        cy.get('.p-card-content .col-12.text-left strong')
            .contains('NFRD required')
            .trigger('mouseleave' )
        cy.get('.p-card-content .col-12.text-left strong')
            .contains('Level of Assurance')
            .trigger('mouseenter', "center")
        cy.get('.p-tooltip').contains(AssuranceText)
    });
})


