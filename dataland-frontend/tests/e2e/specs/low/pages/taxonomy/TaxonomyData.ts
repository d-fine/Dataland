describe('EU Taxonomy Page', function () {
    let idList:any
    it('Retrieve data ID list', () => {
        cy.request('GET', `${Cypress.env("API")}/metadata`).then((response) => {
            idList = response.body.map(function (e:string){
                return parseInt(Object.values(e)[2])
            })
        })
    });
    it('page should be present', function () {
        cy.visit("/companies/"+idList[2]+"/eutaxonomies")
        cy.get('#app').should("exist")
    });
    it('Heading is present', () => {
        cy.get('h2').should("contain", "EU Taxonomy Data")
    });
    it('Search Input field should be present', () => {
        const placeholder = "Search by company name"
        cy.get('input[name=eu_taxonomy_search_input]')
            .should('be.disabled')
            .invoke('attr', 'placeholder').should('contain', placeholder)
    });
    it('Should have button to search all entries', () => {
        cy.get('#eu_taxonomy_search_button')
            .should('not.be.disabled')
            .click({force: true})

        cy.get('h2')
            .should('contain', "Results")
        cy.get('table.p-datatable-table').should('exist')
        cy.get('table.p-datatable-table').contains('th','COMPANY')
        cy.get('table.p-datatable-table').contains('th','SECTOR')
        cy.get('table.p-datatable-table').contains('th','MARKET CAP')
        cy.get('table.p-datatable-table').contains('td','VIEW')
            .contains('a', 'VIEW')
            .click()
            .url().should('include', '/companies/')
            .url().should('include', '/eutaxonomies')
    });
});
