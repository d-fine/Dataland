import { getStoredCompaniesForDataType } from "@e2e/utils/ApiUtils";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { doThingsInChunks, reader_name, reader_pw } from "@e2e/utils/Cypress";
import { Configuration, DataTypeEnum, MetaDataControllerApi, StoredCompany } from "@clients/backend";

const chunkSize = 40;

describe(
  "As a developer, I want to ensure that all tests work by ensuring that all EuTaxonomy data is cached",
  { defaultCommandTimeout: Cypress.env("PREVISIT_TIMEOUT_S") * 1000 },
  () => {
    function visitTaxonomyData(dataType: DataTypeEnum): void {
      getKeycloakToken(reader_name, reader_pw).then((token) => {
        cy.browserThen(getStoredCompaniesForDataType(token, dataType)).then((storedCompanies: StoredCompany[]) =>
          doThingsInChunks(storedCompanies, chunkSize, (storedCompany) =>
            new MetaDataControllerApi(new Configuration({ accessToken: token }))
              .getDataMetaInfo(storedCompany.dataRegisteredByDataland[0].dataId)
              .then((dataGetResponse) => {
                if (dataGetResponse.status !== 200) {
                  assert(
                    dataGetResponse.status === 200,
                    `Got status code ${dataGetResponse.status.toString()} during Previsit of ${String(storedCompany)}`
                  );
                }
              })
          )
        );
      });
    }

    it("Visit all EuTaxonomy Financial", () => {
      visitTaxonomyData(DataTypeEnum.EutaxonomyFinancials);
    });

    it("Visit all EuTaxonomy Non-Financial data", () => {
      visitTaxonomyData(DataTypeEnum.EutaxonomyNonFinancials);
    });
  }
);
