import { doThingsInChunks, admin_name, admin_pw, wrapPromiseToCypressPromise } from "@e2e/utils/Cypress";
import {
  type DataMetaInformation,
  DataTypeEnum,
  type EuTaxonomyDataForFinancials,
  type EuTaxonomyDataForNonFinancials,
  type LksgData,
  type PathwaysToParisData,
  type SfdrData,
  type SmeData,
} from "@clients/backend";
import { countCompaniesAndDataSetsForDataType } from "@e2e//utils/GeneralApiUtils";
import { type FixtureData } from "@sharedUtils/Fixtures";
import { uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { uploadOneEuTaxonomyFinancialsDatasetViaApi } from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { uploadOneEuTaxonomyNonFinancialsDatasetViaApi } from "@e2e/utils/EuTaxonomyNonFinancialsUpload";
import { uploadOneLksgDatasetViaApi } from "@e2e/utils/LksgUpload";
import { uploadOneSfdrDataset } from "@e2e/utils/SfdrUpload";
import { uploadOneSmeDataset } from "@e2e/utils/SmeUpload";
import { describeIf } from "@e2e/support/TestUtility";
import { uploadAllDocuments } from "@e2e/utils/DocumentUpload";
import { uploadOneP2pDatasetViaApi } from "@e2e/utils/P2pUpload";

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
    type UploadFunction<T> = (
      token: string,
      companyId: string,
      reportingPeriod: string,
      dataset: T,
    ) => Promise<DataMetaInformation>;

    /**
     * A higher-level helper function for bulk data upload. Creates all provided companies and uses
     * the uploaderOneFrameworkDataset function to upload the datasets
     * @param fixtureDataForFrameworkT a list of framework-T fixture data with datasets to upload
     * @param uploadOneFrameworkDataset a function that uploads a single dataset
     */
    function prepopulate<T>(
      fixtureDataForFrameworkT: Array<FixtureData<T>>,
      uploadOneFrameworkDataset: UploadFunction<T>,
    ): void {
      cy.getKeycloakToken(admin_name, admin_pw).then((token) => {
        doThingsInChunks(fixtureDataForFrameworkT, chunkSize, async (fixtureData) => {
          const storedCompany = await uploadCompanyViaApi(token, fixtureData.companyInformation);
          await uploadOneFrameworkDataset(token, storedCompany.companyId, fixtureData.reportingPeriod, fixtureData.t);
        });
      });
    }

    /**
     * Uses the Dataland API to verify that the number of companies that contain at least one dataset of the
     * provided data type equal the expected number.
     * It also asserts that the total number of datasets of the provided data type equals that number.
     * @param dataType the datatype to filter by
     * @param expectedNumberOfCompanies is the expected number of companies
     */
    function checkIfNumberOfCompaniesAndDataSetsAreAsExpectedForDataType(
      dataType: DataTypeEnum,
      expectedNumberOfCompanies: number,
    ): void {
      cy.getKeycloakToken(admin_name, admin_pw)
        .then((token) => wrapPromiseToCypressPromise(countCompaniesAndDataSetsForDataType(token, dataType)))
        .then((response) => {
          assert(
            response.numberOfDataSetsForDataType === expectedNumberOfCompanies &&
              response.numberOfCompaniesForDataType === expectedNumberOfCompanies,
            `Found ${response.numberOfCompaniesForDataType} companies having 
            ${response.numberOfDataSetsForDataType} datasets with datatype ${dataType}, 
            but expected ${expectedNumberOfCompanies} companies and ${expectedNumberOfCompanies} datasets`,
          );
        });
    }

    before(function uploadDocumentsAndStoreDocumentIds() {
      cy.getKeycloakToken(admin_name, admin_pw).then((token) => {
        uploadAllDocuments(token);
      });
    });

    describeIf(
      "Upload and validate EuTaxonomy for financials data",
      {
        executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
      },
      () => {
        let fixtureDataForEuTaxonomyFinancials: Array<FixtureData<EuTaxonomyDataForFinancials>>;

        before(function () {
          cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancials").then(function (jsonContent) {
            fixtureDataForEuTaxonomyFinancials = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
          });
        });

        it("Upload eutaxonomy-financials fake-fixtures", () => {
          prepopulate(fixtureDataForEuTaxonomyFinancials, uploadOneEuTaxonomyFinancialsDatasetViaApi);
        });

        it("Checks that all the uploaded company ids and data ids can be retrieved", () => {
          checkIfNumberOfCompaniesAndDataSetsAreAsExpectedForDataType(
            DataTypeEnum.EutaxonomyFinancials,
            fixtureDataForEuTaxonomyFinancials.length,
          );
        });
      },
    );

    describeIf(
      "Upload and validate EuTaxonomy for non-financials data",
      {
        executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
      },
      () => {
        let companiesWithEuTaxonomyDataForNonFinancials: Array<FixtureData<EuTaxonomyDataForNonFinancials>>;

        before(function () {
          cy.fixture("CompanyInformationWithEuTaxonomyDataForNonFinancials").then(function (jsonContent) {
            companiesWithEuTaxonomyDataForNonFinancials = jsonContent as Array<
              FixtureData<EuTaxonomyDataForNonFinancials>
            >;
          });
        });

        it("Upload eutaxonomy-non-financials fake-fixtures", () => {
          prepopulate(companiesWithEuTaxonomyDataForNonFinancials, uploadOneEuTaxonomyNonFinancialsDatasetViaApi);
        });

        it("Checks that all the uploaded company ids and data ids can be retrieved", () => {
          checkIfNumberOfCompaniesAndDataSetsAreAsExpectedForDataType(
            DataTypeEnum.EutaxonomyNonFinancials,
            companiesWithEuTaxonomyDataForNonFinancials.length,
          );
        });
      },
    );

    describeIf(
      "Upload and validate Lksg data",
      {
        executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
      },
      () => {
        let companiesWithLksgData: Array<FixtureData<LksgData>>;

        before(function () {
          cy.fixture("CompanyInformationWithLksgData").then(function (jsonContent) {
            companiesWithLksgData = jsonContent as Array<FixtureData<LksgData>>;
          });
        });

        it("Upload Lksg fake-fixtures", () => {
          prepopulate(companiesWithLksgData, uploadOneLksgDatasetViaApi);
        });

        it("Checks that all the uploaded company ids and data ids can be retrieved", () => {
          checkIfNumberOfCompaniesAndDataSetsAreAsExpectedForDataType(DataTypeEnum.Lksg, companiesWithLksgData.length);
        });
      },
    );

    describeIf(
      "Upload and validate P2p data",
      {
        executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
      },
      () => {
        let companiesWithP2pData: Array<FixtureData<PathwaysToParisData>>;

        before(function () {
          cy.fixture("CompanyInformationWithP2pData").then(function (jsonContent) {
            companiesWithP2pData = jsonContent as Array<FixtureData<PathwaysToParisData>>;
          });
        });

        it("Upload P2p fake-fixtures", () => {
          prepopulate(companiesWithP2pData, uploadOneP2pDatasetViaApi);
        });

        it("Checks that all the uploaded company ids and data ids can be retrieved", () => {
          checkIfNumberOfCompaniesAndDataSetsAreAsExpectedForDataType(DataTypeEnum.P2p, companiesWithP2pData.length);
        });
      },
    );

    describeIf(
      "Upload and validate Sfdr data",
      {
        executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
      },
      () => {
        let companiesWithSfdrData: Array<FixtureData<SfdrData>>;

        before(function () {
          cy.fixture("CompanyInformationWithSfdrData").then(function (jsonContent) {
            companiesWithSfdrData = jsonContent as Array<FixtureData<SfdrData>>;
          });
        });

        it("Upload Sfdr fake-fixtures", () => {
          prepopulate(companiesWithSfdrData, uploadOneSfdrDataset);
        });

        it("Checks that all the uploaded company ids and data ids can be retrieved", () => {
          checkIfNumberOfCompaniesAndDataSetsAreAsExpectedForDataType(DataTypeEnum.Sfdr, companiesWithSfdrData.length);
        });
      },
    );

    describeIf(
      "Upload and validate Sme data",
      {
        executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
      },
      () => {
        let companiesWithSmeData: Array<FixtureData<SmeData>>;

        before(function () {
          cy.fixture("CompanyInformationWithSmeData").then(function (jsonContent) {
            companiesWithSmeData = jsonContent as Array<FixtureData<SmeData>>;
          });
        });

        it("Upload Sme fake-fixtures", () => {
          prepopulate(companiesWithSmeData, uploadOneSmeDataset);
        });

        it("Checks that all the uploaded company ids and data ids can be retrieved", () => {
          checkIfNumberOfCompaniesAndDataSetsAreAsExpectedForDataType(DataTypeEnum.Sme, companiesWithSmeData.length);
        });
      },
    );
  },
);
