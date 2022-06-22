import {doThingsInChunks, getKeycloakToken} from "../../support/utility";

const chunkSize = 40

describe('Visit all EuTaxonomy Data', () => {
    it('Visit all EuTaxonomy Data', () => {
        cy.retrieveDataIdsList().then({timeout: Cypress.env("PREVISIT_TIMEOUT_S") * 1000}, async (dataIdList) => {
            getKeycloakToken("data_reader", Cypress.env("KEYCLOAK_READER_PASSWORD")).then(async (token) => {
                doThingsInChunks(
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
