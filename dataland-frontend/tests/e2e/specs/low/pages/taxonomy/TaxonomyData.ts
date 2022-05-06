describe('EU Taxonomy Page', function () {
    let dataIdList:any
    it('Retrieve data ID list', () => {
        cy.request('GET', `${Cypress.env("API")}/metadata`).then((response) => {
            dataIdList = response.body.map(function (e: any) {
                return e.dataId
            })
        })
    });
    it('page should be present', function () {
        cy.visit("/companies/"+dataIdList[2]+"/eutaxonomies")
        cy.get('#app').should("exist")
    });
    it('Heading is present', () => {
        cy.get('h2').should("contain", "EU Taxonomy Data")
    });
    it('Search Input field should be present', () => {
        const placeholder = "Search a company by name, ISIN, PermID or LEI"
        cy.get('input[name=eu_taxonomy_search_input]')
            .should('not.be.disabled')
            .invoke('attr', 'placeholder').should('contain', placeholder)
    });
});
