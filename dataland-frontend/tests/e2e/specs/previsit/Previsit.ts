describe('Visit all EuTaxonomy Data', () => {
    async function visitAllTaxonomyData(dataIdList: Array<string>) {
        await Promise.all(dataIdList.map(async (dataId: string) => {
                await fetch(`${Cypress.env("API")}/data/eutaxonomies/${dataId}`)
            })
        )
    }

    it('Visit all EuTaxonomy Data', () => {
        cy.retrieveDataIdsList().then(async (dataIdList: Array<string>) => {
            await visitAllTaxonomyData(dataIdList);
        });
    });
});
