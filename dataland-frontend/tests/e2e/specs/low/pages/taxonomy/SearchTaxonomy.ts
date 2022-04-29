describe('Search Taxonomy', function () {
    let companiesData:any
    let idList:any

    before(function(){
        cy.fixture('CompanyInformation').then(function(companies){
            companiesData=companies
        });
    });

    it('Retrieve data ID list', () => {
        cy.request('GET', `${Cypress.env("API")}/metadata`).then((response) => {
            idList = response.body.map(function (e:string){
                return parseInt(Object.values(e)[2])
            })
        })
    });
    it('page should be present', function () {
        cy.visit("/searchtaxonomy")
        cy.get('#app').should("exist")
    });
    it('Heading should be present', () => {
        cy.get('h1').should("contain", "Search EU Taxonomy data")
    });

    it('Search Input field should be present before index filter', () => {
        const placeholder = "Search a company by name, ISIN, PermID or LEI"
        const inputValue = "A company name"
        cy.get('input[name=eu_taxonomy_search_input]')
            .should('not.be.disabled')
            .click({force:true})
            .type(inputValue)
            .should('have.value', inputValue)
            .invoke('attr', 'placeholder').should('contain', placeholder)
    });

    it('Company Search by Name', () => {
        cy.visit('/searchtaxonomy')
        const inputValue = companiesData[0].companyName
        cy.get('input[name=eu_taxonomy_search_input]')
            .should('not.be.disabled')
            .click({force:true})
            .type(inputValue)
            .type('{enter}')
            .should('have.value', inputValue)
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
        cy.get('h1').contains(inputValue)
    });

    it('Company Search by Identifier', () => {
        cy.visit('/searchtaxonomy')
        const inputValue = companiesData[1].identifiers[0].identifierValue
        cy.get('input[name=eu_taxonomy_search_input]')
            .should('not.be.disabled')
            .click({force:true})
            .type(inputValue)
            .type('{enter}')
            .should('have.value', inputValue)
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

    it('Search Input field should be always present', () => {
        const placeholder = "Search a company by name, ISIN, PermID or LEI"
        const inputValue = "A company name"
        cy.visit("/companies/"+idList[7]+"/eutaxonomies")
        cy.get('input[name=eu_taxonomy_search_input]')
            .should('not.be.disabled')
            .click({force:true})
            .type(inputValue)
            .should('have.value', inputValue)
            .invoke('attr', 'placeholder').should('contain', placeholder)
    });


    it('Autocomplete functionality', () => {
        cy.intercept('**/api/companies*').as('searchCompany')
        cy.visit('/searchtaxonomy')
        cy.get('input[name=eu_taxonomy_search_input]')
            .click({force:true})
            .type('b')
        cy.wait('@searchCompany', {timeout: 1000}).then(() => {
            cy.get('.p-autocomplete-item')
                .eq(0).click()
                .url().should('include', '/companies/')
                .url().should('include', '/eutaxonomies')
        })

    });

    it('Scroll functionality', () => {
        cy.visit('/searchtaxonomy')
        cy.get('button[name=search_bar_collapse]').should('not.exist')
        cy.get('input[name=eu_taxonomy_search_input]')
            .click()
            .type('a')
            .type('{enter}')
        cy.scrollTo(0, 500)
        cy.get('input[name=eu_taxonomy_search_input]').should('not.exist')
        cy.get('button[name=search_bar_collapse]').should('exist')

        cy.scrollTo(0, 0)
        cy.get('input[name=eu_taxonomy_search_input]').should('exist')
        cy.get('button[name=search_bar_collapse]').should('not.exist')

        cy.scrollTo(0, 500)
        cy.get('input[name=eu_taxonomy_search_input]').should('not.exist')
        cy.get('button[name=search_bar_collapse]').should('exist')
            .click()
        cy.get('input[name=eu_taxonomy_search_input]').should('exist')
        cy.get('button[name=search_bar_collapse]').should('not.exist')
    });




});
