describe.only('Tutorialspoint Test', function () {
    let eutaxonomiesData:any
    let companiesData:any
    before(function(){
        cy.fixture('eutaxonomies').then(function(eutaxonomies){
            eutaxonomiesData=eutaxonomies
        });
        cy.fixture('companies').then(function(companies){
            companiesData=companies
        });

    });

    it('Populate Companies', function (){
        for (const index in companiesData) {
            cy.request('POST', 'http://localhost:8080/api/companies', companiesData[index])
        }
        console.log(companiesData)
    });

    it('Populate EU Taxonomy Data', function (){
        for (const index in eutaxonomiesData) {
            cy.request('POST', `http://localhost:8080/api/eutaxonomies/${Number(index) + 1}`, eutaxonomiesData[index])
        }
        console.log(eutaxonomiesData)
    });
});