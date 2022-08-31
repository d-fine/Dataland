import { performSimpleGet } from "../../utils/ApiUtils";
import { getKeycloakToken } from "../../utils/Auth";
import { doThingsInChunks } from "../../utils/Cypress";

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
              assert(
                dataGetResponse.status.toString() === "200",
                `Got status code ${dataGetResponse.status.toString()} during Previsit of ${element}`
              );
            })
          );
        });
      });
    });
  }
);
