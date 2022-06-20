import {doThingsInChunks, getKeycloakToken} from "../../support/utility";

const chunkSize = 40

describe('Visit all EuTaxonomy Data', () => {
    it('Visit all EuTaxonomy Data', () => {
        cy.retrieveDataIdsList().then({timeout: Cypress.env("PREVISIT_TIMEOUT_S") * 1000}, async (dataIdList) => {
            getKeycloakToken("some_user", "test").then(async (token) => {
                await doThingsInChunks(
                    dataIdList,
                    chunkSize,
                    dataId => fetch(
                        `${Cypress.env("API")}/data/eutaxonomies/${dataId}`,
                        {
                            headers: {
                                'Authorization': 'Bearer ' + token
                            }
                        })
                )
            })
        });
    });
});
