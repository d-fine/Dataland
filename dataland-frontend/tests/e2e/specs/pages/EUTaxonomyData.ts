describe('EU Taxo Data testsuite', () => {
    it('Check if App is present', () => {
        cy.visit("/eutaxonomies")
        cy.get('#app').should('exist')
    });
    it('Table should be present', () => {
        cy.get('table h4').should("have.text","Available datasets")
    });
    it('Table headings present', () => {
        cy.get('table th').contains("Data ID")
        cy.get('table th').contains("Data Type")
        cy.get('table th').contains("Link")
    });

})



describe.only('Tutorialspoint Test', function () {
    let eutaxonomiesData:any
    before(function(){
        cy.fixture('eutaxonomies').then(function(eutaxonomies){
            eutaxonomiesData=eutaxonomies
        });
    });

    it('Test Case1', function (){
        cy.visit("/upload")
        cy.get('input[name=companyID]')
            .type("1")
        cy.get('div[title=capex] input[name=aligned_turnover]').type(eutaxonomiesData[0].Capex.aligned_turnover)
        cy.request('POST', 'http://localhost:8080/api/eutaxonomies/1', eutaxonomiesData[0])
        console.log(eutaxonomiesData)
    });
});