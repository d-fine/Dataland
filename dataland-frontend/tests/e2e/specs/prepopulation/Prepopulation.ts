import { doThingsInChunks, admin_name, admin_pw, wrapPromiseToCypressPromise } from "@e2e/utils/Cypress";
import { countCompaniesAndDataSetsForDataType } from "@e2e//utils/GeneralApiUtils";
import { FixtureData } from "@sharedUtils/Fixtures";
import { uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { describeIf } from "@e2e/support/TestUtility";
import { uploadAllDocuments } from "@e2e/utils/DocumentUpload";
import { FrameworkDataTypes, uploadFrameworkData } from "@e2e/utils/FrameworkUpload";
import { frameworkFixtureMap } from "@e2e/utils/FixtureMap";

const chunkSize = 15;

describe(
  "As a user, I want to be able to see some data on the Dataland webpage",
  {
    defaultCommandTimeout: Cypress.env("prepopulate_timeout_s") * 1000,
    retries: {
      runMode: 0,
      openMode: 0,
    },
  },

  () => {
    before(function uploadDocumentsAndStoreDocumentIds() {
      cy.getKeycloakToken(admin_name, admin_pw).then((token) => {
        uploadAllDocuments(token);
      });
    });

    /**
     * A meta-programming function that allows the registration of a new framework for prepopulation
     * @param framework The framework to prepopulate
     * @param fixtureJson The name of the Json file containing the fixtures
     */
    function registerFrameworkFakeFixtureUpload<K extends keyof FrameworkDataTypes>(
      framework: K,
      fixtureJson: string,
    ): void {
      describeIf(
        `Upload and validate data for framework ${framework}`,
        {
          executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
        },
        () => {
          let fixtureData: Array<FixtureData<FrameworkDataTypes[K]["data"]>>;

          before(function () {
            cy.fixture(fixtureJson).then(function (jsonContent) {
              fixtureData = jsonContent as typeof fixtureData;
            });
          });

          it(`Upload data for framework ${framework}`, () => {
            cy.getKeycloakToken(admin_name, admin_pw).then((token) => {
              doThingsInChunks(fixtureData, chunkSize, async (fixtureDataClosure) => {
                const storedCompany = await uploadCompanyViaApi(token, fixtureDataClosure.companyInformation);
                await uploadFrameworkData(
                  framework,
                  token,
                  storedCompany.companyId,
                  fixtureDataClosure.reportingPeriod,
                  fixtureDataClosure.t,
                );
              });
            });
          });

          it("Checks that all the uploaded company ids and data ids can be retrieved", () => {
            const expectedNumberOfCompanies = fixtureData.length;
            cy.getKeycloakToken(admin_name, admin_pw)
              .then((token) => wrapPromiseToCypressPromise(countCompaniesAndDataSetsForDataType(token, framework)))
              .then((response) => {
                assert(
                  response.numberOfDataSetsForDataType === expectedNumberOfCompanies &&
                    response.numberOfCompaniesForDataType === expectedNumberOfCompanies,
                  `Found ${response.numberOfCompaniesForDataType} companies having 
            ${response.numberOfDataSetsForDataType} datasets with datatype ${framework}, 
            but expected ${expectedNumberOfCompanies} companies and ${expectedNumberOfCompanies} datasets`,
                );
              });
          });
        },
      );
    }

    for (const [key, value] of Object.entries(frameworkFixtureMap)) {
      const keyTyped = key as keyof typeof frameworkFixtureMap;
      registerFrameworkFakeFixtureUpload(keyTyped, value);
    }
  },
);
