import { doThingsInChunks, wrapPromiseToCypressPromise } from "@e2e/utils/Cypress";
import {
  CompanyInformation,
  EuTaxonomyDataForNonFinancials,
  EuTaxonomyDataForFinancials,
  Configuration,
  EuTaxonomyDataForFinancialsControllerApi,
  EuTaxonomyDataForNonFinancialsControllerApi,
  DataTypeEnum,
} from "@clients/backend";
import { countCompanyAndDataIds, uploadCompany } from "@e2e/utils/ApiUtils";
const chunkSize = 15;

describe(
  "As a user, I want to be able to see some data on the DataLand webpage",
  {
    defaultCommandTimeout: Cypress.env("PREPOPULATE_TIMEOUT_S") * 1000,
    retries: {
      runMode: 0,
      openMode: 0,
    },
  },
  () => {
    describe("Upload and validate EuTaxonomy for financials data", () => {
      let companiesWithEuTaxonomyDataForFinancials: Array<{
        companyInformation: CompanyInformation;
        t: EuTaxonomyDataForFinancials;
      }>;

      before(function () {
        cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancials").then(function (companies) {
          companiesWithEuTaxonomyDataForFinancials = companies;
        });
      });

      it("Upload eutaxonomy-financials fake-fixtures", () => {
        async function uploadEuTaxonomyFinancialsData(
          token: string,
          companyId: string,
          data: EuTaxonomyDataForFinancials
        ): Promise<void> {
          await new EuTaxonomyDataForFinancialsControllerApi(
            new Configuration({ accessToken: token })
          ).postCompanyAssociatedData1({
            companyId,
            data,
          });
        }
        cy.getKeycloakToken("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD")).then((token) => {
          doThingsInChunks(companiesWithEuTaxonomyDataForFinancials, chunkSize, async (it) => {
            const storedCompany = await uploadCompany(token, it.companyInformation);
            await uploadEuTaxonomyFinancialsData(token, storedCompany.companyId, it.t);
          });
        });
      });
      it("Checks that all the uploaded company ids and data ids can be retrieved", () => {
        cy.getKeycloakToken("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD"))
          .then((token) =>
            wrapPromiseToCypressPromise(countCompanyAndDataIds(token, DataTypeEnum.EutaxonomyFinancials))
          )
          .then((response) => {
            assert(
              response.matchingDataIds === companiesWithEuTaxonomyDataForFinancials.length &&
                response.matchingCompanies === companiesWithEuTaxonomyDataForFinancials.length,
              `Found ${response.matchingCompanies} companies with matching data and ${response.matchingDataIds} uploaded data ids, expected both to be ${companiesWithEuTaxonomyDataForFinancials.length}`
            );
          });
      });
    });

    describe("Upload and validate EuTaxonomy for non-financials data", () => {
      let companiesWithEuTaxonomyDataForNonFinancials: Array<{
        companyInformation: CompanyInformation;
        t: EuTaxonomyDataForNonFinancials;
      }>;

      before(function () {
        cy.fixture("CompanyInformationWithEuTaxonomyDataForNonFinancials").then(function (companies) {
          companiesWithEuTaxonomyDataForNonFinancials = companies;
        });
      });

      it("Upload eutaxonomy-non-financials fake-fixtures", () => {
        async function uploadEuTaxonomyNonFinancialsData(
          token: string,
          companyId: string,
          data: EuTaxonomyDataForFinancials
        ): Promise<void> {
          await new EuTaxonomyDataForNonFinancialsControllerApi(
            new Configuration({ accessToken: token })
          ).postCompanyAssociatedData({
            companyId,
            data,
          });
        }
        cy.getKeycloakToken("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD")).then((token) => {
          doThingsInChunks(companiesWithEuTaxonomyDataForNonFinancials, chunkSize, async (it) => {
            const storedCompany = await uploadCompany(token, it.companyInformation);
            await uploadEuTaxonomyNonFinancialsData(token, storedCompany.companyId, it.t);
          });
        });
      });
      it("Checks that all the uploaded company ids and data ids can be retrieved", () => {
        cy.getKeycloakToken("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD"))
          .then((token) =>
            wrapPromiseToCypressPromise(countCompanyAndDataIds(token, DataTypeEnum.EutaxonomyNonFinancials))
          )
          .then((response) => {
            assert(
              response.matchingDataIds === companiesWithEuTaxonomyDataForNonFinancials.length &&
                response.matchingCompanies === companiesWithEuTaxonomyDataForNonFinancials.length,
              `Found ${response.matchingCompanies} companies with matching data and ${response.matchingDataIds} uploaded data ids, expected both to be ${companiesWithEuTaxonomyDataForNonFinancials.length}`
            );
          });
      });
    });
  }
);
