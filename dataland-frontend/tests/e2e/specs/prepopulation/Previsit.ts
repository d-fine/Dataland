import { performSimpleGet } from "@e2e/utils/ApiUtils";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { doThingsInChunks } from "@e2e/utils/Cypress";

const chunkSize = 40;

describe(
  "As a developer, I want to ensure that all tests work by ensuring that all EuTaxonomy data is cached",
  { defaultCommandTimeout: Cypress.env("PREVISIT_TIMEOUT_S") * 1000 },
  () => {
    it("Visit all EuTaxonomy Data", () => {
      performSimpleGet("metadata").then((metaDataResponse) => {
        getKeycloakToken("data_reader", Cypress.env("KEYCLOAK_READER_PASSWORD")).then(async (token) => {
          doThingsInChunks(metaDataResponse.body, chunkSize, (element: any) =>
            fetch(`/api/data/${element.dataType}/${element.dataId}`, {
              headers: {
                Authorization: "Bearer " + token,
              },
            }).then((dataGetResponse) => {
              // Introduced if to reduce number of unnecessary asserts which add some overhead as coverage is re-computed after
              // every assert
              if (dataGetResponse.status !== 200) {
                assert(
                  dataGetResponse.status === 200,
                  `Got status code ${dataGetResponse.status.toString()} during Previsit of ${element}`
                );
              }
            })
          );
        });
      });
    });
  }
);
