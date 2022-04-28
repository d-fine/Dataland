describe('Back Button test suite', () => {
    let idList:any
    it('Retrieve data ID list', () => {
        cy.request('GET', `${Cypress.env("API")}/metadata`).then((response) => {
            idList = response.body.map(function (e:string){
                return parseInt(Object.values(e)[2])
            })
        })
    });
    it('company eu taxonomy page should be present and contain back button', function () {
        cy.visit("/searchtaxonomy")
        cy.visit("/companies/"+idList[5]+"/eutaxonomies")
        cy.get('#app').should("exist")
        cy.get('span.text-primary[title=back_button]')
            .parent('.cursor-pointer.grid.align-items-center')
            .click()
            .url().should('include', '/searchtaxonomy')

    });
})


