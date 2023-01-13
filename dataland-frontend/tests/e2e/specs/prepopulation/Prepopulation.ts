import { doThingsInChunks, wrapPromiseToCypressPromise, uploader_pw, uploader_name } from "@e2e/utils/Cypress";
import {
  EuTaxonomyDataForNonFinancials,
  EuTaxonomyDataForFinancials,
  DataTypeEnum,
  LksgData,
  SfdrData,
  SmeData,
  CompanyDataControllerApi,
  Configuration,
} from "@clients/backend";
import { countCompaniesAndDataSetsForDataType } from "@e2e/utils/ApiUtils";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";
import { uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { uploadOneEuTaxonomyFinancialsDatasetViaApi } from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { uploadOneEuTaxonomyNonFinancialsDatasetViaApi } from "@e2e/utils/EuTaxonomyNonFinancialsUpload";
import { uploadOneLksgDatasetViaApi } from "@e2e/utils/LksgUpload";
import { uploadOneSfdrDataset } from "@e2e/utils/SfdrUpload";
import { uploadOneSmeDataset } from "@e2e/utils/SmeUpload";
import { describeIf } from "@e2e/support/TestUtility";
import { generateLksgData } from "../../fixtures/lksg/LksgDataFixtures";
const chunkSize = 15;

describe(
  "As a user, I want to be able to see some data on the Dataland webpage",
  {
    defaultCommandTimeout: Cypress.env("PREPOPULATE_TIMEOUT_S") * 1000,
    retries: {
      runMode: 0,
      openMode: 0,
    },
  },

  () => {
    type UploadFunction<T> = (token: string, companyId: string, dataset: T) => Promise<void>;

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

    function checkMatchingIds(
      dataType: DataTypeEnum,
      expectedNumberOfCompanyIds: number,
      expectedNumberOfDataSetIds: number
    ): void {
      cy.getKeycloakToken(uploader_name, uploader_pw)
        .then((token) => wrapPromiseToCypressPromise(countCompaniesAndDataSetsForDataType(token, dataType)))
        .then((response) => {
          assert(
            response.numberOfDataSetsForDataType === expectedNumberOfDataSetIds &&
              response.numberOfCompaniesForDataType === expectedNumberOfCompanyIds,
            `Found ${response.numberOfCompaniesForDataType} companies with matching data 
                  and ${response.numberOfDataSetsForDataType} uploaded data ids, expected ${expectedNumberOfCompanyIds} 
                  company ids and ${expectedNumberOfDataSetIds} data set ids`
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
        checkMatchingIds(
          DataTypeEnum.EutaxonomyFinancials,
          companiesWithEuTaxonomyDataForFinancials.length,
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
        checkMatchingIds(
          DataTypeEnum.EutaxonomyNonFinancials,
          companiesWithEuTaxonomyDataForNonFinancials.length,
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
          checkMatchingIds(DataTypeEnum.Lksg, companiesWithLksgData.length, companiesWithLksgData.length);
        });

        it("Upload an additional lksg data set to existing fake-fixtures", () => {
          let preparedFixtures: Array<FixtureData<LksgData>>;
          cy.fixture("CompanyInformationWithLksgPreparedFixtures")
            .then(function (jsonContent) {
              preparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
            })
            .then(() => {
              prepopulate(preparedFixtures, uploadOneLksgDatasetViaApi);
            });
          cy.getKeycloakToken(uploader_name, uploader_pw).then(async (token) => {
            const preparedCompanyId = (
              await new CompanyDataControllerApi(new Configuration({ accessToken: token })).getCompanies(
                "two-lksg-data-sets"
              )
            ).data[0].companyId;
            const dataSet = generateLksgData();
            await uploadOneLksgDatasetViaApi(token, preparedCompanyId, dataSet);
          });
        });

        it("Checks that all the uploaded company ids and data ids can be retrieved", () => {
          checkMatchingIds(DataTypeEnum.Lksg, companiesWithLksgData.length + 1, companiesWithLksgData.length + 2);
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
          checkMatchingIds(DataTypeEnum.Sfdr, companiesWithSfdrData.length, companiesWithSfdrData.length);
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
          checkMatchingIds(DataTypeEnum.Sme, companiesWithSmeData.length, companiesWithSmeData.length);
        });
      }
    );
  }
);
