import {doThingsInChunks} from "../../support/utility";

const chunkSize = 40

describe('Visit all EuTaxonomy Data', () => {
    it('Visit all EuTaxonomy Data', () => {
        cy.retrieveDataIdsList().then({timeout: 20 * 60 * 1000}, async (dataIdList) => {
            await doThingsInChunks(
                dataIdList,
                chunkSize,
                dataId => fetch(`${Cypress.env("API")}/data/eutaxonomies/${dataId}`))
        });
    });
});
