import { retrieveDataIdsList } from "../../utils/ApiUtils";
import { getKeycloakToken } from "../../utils/Auth";
import { doThingsInChunks } from "../../utils/Cypress";

const chunkSize = 40;

describe(
  "As a developer, I want to ensure that all tests work by ensuring that all EuTaxonomy data is cached",
  { defaultCommandTimeout: Cypress.env("PREVISIT_TIMEOUT_S") * 1000 },
  () => {
    it("Visit all EuTaxonomy Data", () => {
      retrieveDataIdsList().then(async (dataIdList) => {
        getKeycloakToken("data_reader", Cypress.env("KEYCLOAK_READER_PASSWORD")).then(async (token) => {
          doThingsInChunks(dataIdList, chunkSize, (dataId) =>
            fetch(`${Cypress.env("API")}/data/eutaxonomies/${dataId}`, {
              headers: {
                Authorization: "Bearer " + token,
              },
            })
          );
        });
      });
    });
  }
);
