describe.only('Test that if unauthenticated will be redirected to landing page', () => {
    it('test for each of given paths', () => {
        const pages = ['/upload', '/search', '/searchtaxonomy', '/companies/:companyID/eutaxonomies']
        pages.forEach(page => {
            cy.visit(page)
            cy.get('h1').should("contain.text","CREATE A DATASET")
        })
    })
})