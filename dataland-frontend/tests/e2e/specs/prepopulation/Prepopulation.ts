import { doThingsInChunks, wrapPromiseToCypressPromise, uploader_pw, uploader_name } from "@e2e/utils/Cypress";
import {
  EuTaxonomyDataForNonFinancials,
  EuTaxonomyDataForFinancials,
  DataTypeEnum,
  LksgData,
  SfdrData,
  SmeData,
  DataMetaInformation,
} from "@clients/backend";
import { countCompaniesAndDataSetsForDataType } from "@e2e//utils/GeneralApiUtils";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";
import { uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { uploadOneEuTaxonomyFinancialsDatasetViaApi } from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { uploadOneEuTaxonomyNonFinancialsDatasetViaApi } from "@e2e/utils/EuTaxonomyNonFinancialsUpload";
import { uploadOneLksgDatasetViaApi } from "../../utils/LksgUpload";
import { uploadOneSfdrDataset } from "@e2e/utils/SfdrUpload";
import { uploadOneSmeDataset } from "@e2e/utils/SmeUpload";
import { describeIf } from "@e2e/support/TestUtility";
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
    type UploadFunction<T> = (token: string, companyId: string, dataset: T) => Promise<DataMetaInformation>;

    /**
     * A higher-level helper function for bulk data upload. Creates all provided companies and uses
     * the uploaderOneFrameworkDataset function to upload the datasets
     *
     * @param companiesWithFrameworkData a list of companies with datasets to upload
     * @param uploadOneFrameworkDataset a function that uploads a single dataset
     */
    function prepopulate<T>(
      companiesWithFrameworkData: Array<FixtureData<T>>,
      uploadOneFrameworkDataset: UploadFunction<T>
    ): void {
      cy.getKeycloakToken(uploader_name, uploader_pw).then((token) => {
        doThingsInChunks(companiesWithFrameworkData, chunkSize, async (it) => {
          const storedCompany = await uploadCompanyViaApi(token, it.companyInformation);
          await uploadOneFrameworkDataset(token, storedCompany.companyId, it.t);
        });
      });
    }

    /**
     * Uses the Dataland API to verify that the number of companies that contain at least one dataset of the
     * provided data type equal the expected number.
     * It also asserts that the total number of datasets of the provided data type equals that number.
     *
     * @param dataType the datatype to filter by
     * @param expectedNumberOfCompanies is the expected number of companies
     */
    function checkIfNumberOfCompaniesAndDataSetsAreAsExpectedForDataType(
      dataType: DataTypeEnum,
      expectedNumberOfCompanies: number
    ): void {
      cy.getKeycloakToken(uploader_name, uploader_pw)
        .then((token) => wrapPromiseToCypressPromise(countCompaniesAndDataSetsForDataType(token, dataType)))
        .then((response) => {
          assert(
            response.numberOfDataSetsForDataType === expectedNumberOfCompanies &&
              response.numberOfCompaniesForDataType === expectedNumberOfCompanies,
            `Found ${response.numberOfCompaniesForDataType} companies having 
            ${response.numberOfDataSetsForDataType} datasets with datatype ${dataType}, 
            but expected ${expectedNumberOfCompanies} companies and ${expectedNumberOfCompanies} datasets`
          );
        });
    }

    describe("Upload and validate EuTaxonomy for financials data", () => {
      let companiesWithEuTaxonomyDataForFinancials: Array<FixtureData<EuTaxonomyDataForFinancials>>;

      before(function () {
        cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancials").then(function (jsonContent) {
          companiesWithEuTaxonomyDataForFinancials = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
        });
      });

      it("Upload eutaxonomy-financials fake-fixtures", () => {
        prepopulate(companiesWithEuTaxonomyDataForFinancials, uploadOneEuTaxonomyFinancialsDatasetViaApi);
      });

      it("Checks that all the uploaded company ids and data ids can be retrieved", () => {
        checkIfNumberOfCompaniesAndDataSetsAreAsExpectedForDataType(
          DataTypeEnum.EutaxonomyFinancials,
          companiesWithEuTaxonomyDataForFinancials.length
        );
      });
    });

    describe("Upload and validate EuTaxonomy for non-financials data", () => {
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
          companiesWithEuTaxonomyDataForNonFinancials.length
        );
      });
    });

    describeIf(
      "Upload and validate Lksg data",
      {
        executionEnvironments: ["developmentLocal", "ci", "developmentCd", "previewCd"],
        dataEnvironments: ["fakeFixtures"],
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
      }
    );

    describeIf(
      "Upload and validate Sfdr data",
      {
        executionEnvironments: ["developmentLocal", "ci", "developmentCd", "previewCd"],
        dataEnvironments: ["fakeFixtures"],
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
      }
    );

    describeIf(
      "Upload and validate Sme data",
      {
        executionEnvironments: ["developmentLocal", "ci", "developmentCd", "previewCd"],
        dataEnvironments: ["fakeFixtures"],
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
      }
    );
  }
);
