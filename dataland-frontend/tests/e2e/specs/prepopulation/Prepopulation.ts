import { doThingsInChunks, wrapPromiseToCypressPromise } from "@e2e/utils/Cypress";
import {
  CompanyInformation,
  EuTaxonomyDataForNonFinancials,
  EuTaxonomyDataForFinancials,
  Configuration,
  EuTaxonomyDataForFinancialsControllerApi,
  EuTaxonomyDataForNonFinancialsControllerApi,
  DataTypeEnum,
  StoredCompany,
  CompanyDataControllerApi,
  LKSGData,
  LksgDataControllerApi,
} from "@clients/backend";
import { countCompanyAndDataIds } from "@e2e/utils/ApiUtils";
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
    async function uploadOneCompany(token: string, companyInformation: CompanyInformation): Promise<StoredCompany> {
      const data = await new CompanyDataControllerApi(new Configuration({ accessToken: token })).postCompany(
        companyInformation
      );
      return data.data;
    }

    function prepopulate(companiesWithEuTaxonomyData: Array<any>, uploadOneEuTaxonomyDataset: Function) {
      cy.getKeycloakToken("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD")).then((token) => {
        doThingsInChunks(companiesWithEuTaxonomyData, chunkSize, async (it) => {
          const storedCompany = await uploadOneCompany(token, it.companyInformation);
          await uploadOneEuTaxonomyDataset(token, storedCompany.companyId, it.t);
        });
      });
    }

    function checkMatchingIds(dataType: DataTypeEnum, expectedNumberOfIds: number) {
      cy.getKeycloakToken("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD"))
        .then((token) => wrapPromiseToCypressPromise(countCompanyAndDataIds(token, dataType)))
        .then((response) => {
          assert(
            response.matchingDataIds === expectedNumberOfIds && response.matchingCompanies === expectedNumberOfIds,
            `Found ${response.matchingCompanies} companies with matching data 
                  and ${response.matchingDataIds} uploaded data ids, expected both to be ${expectedNumberOfIds}`
          );
        });
    }

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
        async function uploadOneEuTaxonomyFinancialsDataset(
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
        prepopulate(companiesWithEuTaxonomyDataForFinancials, uploadOneEuTaxonomyFinancialsDataset);
      });

      it("Checks that all the uploaded company ids and data ids can be retrieved", () => {
        checkMatchingIds(DataTypeEnum.EutaxonomyFinancials, companiesWithEuTaxonomyDataForFinancials.length);
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
        async function uploadOneEuTaxonomyNonFinancialsDataset(
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
        prepopulate(companiesWithEuTaxonomyDataForNonFinancials, uploadOneEuTaxonomyNonFinancialsDataset);
      });

      it("Checks that all the uploaded company ids and data ids can be retrieved", () => {
        checkMatchingIds(DataTypeEnum.EutaxonomyNonFinancials, companiesWithEuTaxonomyDataForNonFinancials.length);
      });
    });

    describe("Upload and validate LKSG data", () => {
      let companiesWithLKSGdata: Array<{
        companyInformation: CompanyInformation;
        t: LKSGData;
      }>;

      before(function () {
        cy.fixture("CompanyInformationWithLKSGdata").then(function (companies) {
          companiesWithLKSGdata = companies;
        });
      });

      it("Upload LKSG fake-fixtures", () => {
        async function uploadOneLKSGDataset(token: string, companyId: string, data: LKSGData): Promise<void> {
          await new LksgDataControllerApi(new Configuration({ accessToken: token })).postCompanyAssociatedData({
            companyId,
            data,
          });
        }
        prepopulate(companiesWithLKSGdata, uploadOneLKSGDataset);
      });

      it("Checks that all the uploaded company ids and data ids can be retrieved", () => {
        checkMatchingIds(DataTypeEnum.Lksg, companiesWithLKSGdata.length);
      });
    });
  }
);
