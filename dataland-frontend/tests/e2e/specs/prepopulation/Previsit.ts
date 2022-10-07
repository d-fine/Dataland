import { getCompanyAndDataIds } from "@e2e/utils/ApiUtils";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { doThingsInChunks } from "@e2e/utils/Cypress";
import { Configuration, DataTypeEnum, MetaDataControllerApi } from "@clients/backend";

const chunkSize = 40;

describe(
  "As a developer, I want to ensure that all tests work by ensuring that all EuTaxonomy data is cached",
  { defaultCommandTimeout: Cypress.env("PREVISIT_TIMEOUT_S") * 1000 },
  () => {
    it("Visit all EuTaxonomy Financial", () => {
      getKeycloakToken("data_reader", Cypress.env("KEYCLOAK_READER_PASSWORD")).then((token) => {
        cy.browserThen(getCompanyAndDataIds(token, DataTypeEnum.EutaxonomyFinancials)).then((datasetFinancial) =>
          doThingsInChunks(datasetFinancial, chunkSize, (element) =>
            new MetaDataControllerApi(new Configuration({ accessToken: token }))
              .getDataMetaInfo(element.dataRegisteredByDataland[0].dataId)
              .then((dataGetResponse) => {
                if (dataGetResponse.status !== 200) {
                  assert(
                    dataGetResponse.status === 200,
                    `Got status code ${dataGetResponse.status.toString()} during Previsit of ${element}`
                  );
                }
              })
          )
        );
      });
    });

    it("Visit all EuTaxonomy Non-Financial data", () => {
      getKeycloakToken("data_reader", Cypress.env("KEYCLOAK_READER_PASSWORD")).then((token) => {
        cy.browserThen(getCompanyAndDataIds(token, DataTypeEnum.EutaxonomyNonFinancials)).then((datasetNonFinancial) =>
          doThingsInChunks(datasetNonFinancial, chunkSize, (element) =>
            new MetaDataControllerApi(new Configuration({ accessToken: token }))
              .getDataMetaInfo(element.dataRegisteredByDataland[0].dataId)
              .then((dataGetResponse) => {
                if (dataGetResponse.status !== 200) {
                  assert(
                    dataGetResponse.status === 200,
                    `Got status code ${dataGetResponse.status.toString()} during Previsit of ${element}`
                  );
                }
              })
          )
        );
      });
    });
  }
);
