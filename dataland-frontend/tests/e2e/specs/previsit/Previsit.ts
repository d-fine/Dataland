describe('Visit all EuTaxonomy Data', () => {
    function visitAllTaxonomyData(dataIdList: Array<string>) {
        return Promise.all(dataIdList.map((dataId: string) => {
                fetch(`${Cypress.env("API")}/data/eutaxonomies/${dataId}`)
            })
        )
    }

    it('Visit all EuTaxonomy Data', () => {
        cy.retrieveDataIdsList().then({timeout: 600*1000}, async (dataIdList: Array<string>) => {
            await visitAllTaxonomyData(dataIdList);
        });
    });
});
