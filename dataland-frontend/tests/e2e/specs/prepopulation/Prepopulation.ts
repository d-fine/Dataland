import { doThingsInChunks, admin_name, admin_pw, wrapPromiseToCypressPromise } from '@e2e/utils/Cypress';
import { countCompaniesAndDataSetsForDataType } from '@e2e//utils/GeneralApiUtils';
import { type FixtureData } from '@sharedUtils/Fixtures';
import { uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { describeIf } from '@e2e/support/TestUtility';
import { uploadAllDocuments } from '@e2e/utils/DocumentUpload';
import {
  type PublicApiClientConstructor,
  uploadGenericFrameworkData,
  uploadVsmeFrameworkData,
} from '@e2e/utils/FrameworkUpload';
import { frameworkFixtureMap } from '@e2e/utils/FixtureMap';
import {
  getAllPublicFrameworkIdentifiers,
  getBasePublicFrameworkDefinition,
} from '@/frameworks/BasePublicFrameworkRegistry';
import { DataTypeEnum, type VsmeData } from '@clients/backend';
import { getUnifiedFrameworkDataControllerFromConfiguration } from '@/utils/api/FrameworkApiClient';
import { convertKebabCaseToPascalCase } from '@/utils/StringFormatter';

const chunkSize = 15;

describe(
  'As a user, I want to be able to see some data on the Dataland webpage',
  {
    defaultCommandTimeout: Cypress.env('prepopulate_timeout_s') * 1000,
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
     * Checks that all the uploaded company ids and data ids can be retrieved
     * @param frameworkIdentifier The framework to check
     * @param expectedNumberOfCompanies The expected number of companies
     */
    function checkUploadedData(frameworkIdentifier: DataTypeEnum, expectedNumberOfCompanies: number): void {
      it(
        'Checks that all the uploaded company ids and data ids can be retrieved',
        {
          retries: {
            runMode: 5,
            openMode: 5,
          },
        },
        () => {
          cy.getKeycloakToken(admin_name, admin_pw)
            .then((token) =>
              wrapPromiseToCypressPromise(countCompaniesAndDataSetsForDataType(token, frameworkIdentifier))
            )
            .then((response) => {
              assert(
                response.numberOfDataSetsForDataType === expectedNumberOfCompanies &&
                  response.numberOfCompaniesForDataType === expectedNumberOfCompanies,
                `Found ${response.numberOfCompaniesForDataType} companies having 
          ${response.numberOfDataSetsForDataType} datasets with datatype ${frameworkIdentifier}, 
          but expected ${expectedNumberOfCompanies} companies and ${expectedNumberOfCompanies} datasets`
              );
            });
        }
      );
    }

    /**
     * A meta-programming function that allows the registration of a new framework for prepopulation
     * @param frameworkIdentifier The framework to prepopulate
     * @param apiClientConstructor a function for constructing an API client fitting for the framework
     * @param nameOfFixtureJson The name of the Json file containing the fixtures
     */
    function registerFrameworkFakeFixtureUpload<FrameworkDataType>(
      frameworkIdentifier: DataTypeEnum,
      apiClientConstructor: PublicApiClientConstructor<FrameworkDataType>,
      nameOfFixtureJson: string
    ): void {
      describeIf(
        `Upload and validate data for framework ${frameworkIdentifier}`,
        {
          executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
        },
        () => {
          let fixtureData: Array<FixtureData<FrameworkDataType>> = [];

          before(function () {
            cy.fixture(nameOfFixtureJson).then(function (jsonContent) {
              fixtureData = jsonContent as typeof fixtureData;
            });
          });

          it(`Upload data for framework ${frameworkIdentifier}`, () => {
            cy.getKeycloakToken(admin_name, admin_pw).then((token) => {
              doThingsInChunks(fixtureData, chunkSize, async (fixtureDataClosure) => {
                const storedCompany = await uploadCompanyViaApi(token, fixtureDataClosure.companyInformation);
                await uploadGenericFrameworkData(
                  token,
                  storedCompany.companyId,
                  fixtureDataClosure.reportingPeriod,
                  fixtureDataClosure.t,
                  apiClientConstructor
                );
              });
            });
          });
          it('Checks that all the uploaded company ids and data ids can be retrieved', function () {
            checkUploadedData(frameworkIdentifier, fixtureData.length);
          });
        }
      );
    }

    /**
     * Uploads sme fixtures
     * @param chunkSize to define how many upload-requests shall be awaited before the next chunk is being uploaded
     * @param numberOfVsmeFixturesToUpload to define how many vsme fixture datasets shall be uploaded
     */
    function uploadVsmeFixtures(chunkSize: number, numberOfVsmeFixturesToUpload: number): void {
      describeIf(
        `Upload and validate data for framework ${DataTypeEnum.Vsme}`,
        {
          executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
        },
        () => {
          let fixtureData: Array<FixtureData<VsmeData>> = [];

          before(function () {
            cy.fixture('CompanyInformationWithVsmeData').then(function (jsonContent) {
              fixtureData = (jsonContent as typeof fixtureData).slice(0, numberOfVsmeFixturesToUpload);
            });
          });

          it(`Upload data for framework ${DataTypeEnum.Vsme}`, () => {
            cy.getKeycloakToken(admin_name, admin_pw).then((token) => {
              doThingsInChunks(fixtureData, chunkSize, async (fixtureDataClosure) => {
                const storedCompany = await uploadCompanyViaApi(token, fixtureDataClosure.companyInformation);
                await uploadVsmeFrameworkData(
                  token,
                  storedCompany.companyId,
                  fixtureDataClosure.reportingPeriod,
                  fixtureDataClosure.t,
                  []
                );
              });
            });
          });
          it('Checks that all the uploaded company ids and data ids can be retrieved', function () {
            checkUploadedData(DataTypeEnum.Vsme, fixtureData.length);
          });
        }
      );
    }

    // Prepopulation for frameworks not implemented with the framework-toolbox
    for (const [key, value] of Object.entries(frameworkFixtureMap)) {
      const keyTyped = key as keyof typeof frameworkFixtureMap;
      registerFrameworkFakeFixtureUpload(
        keyTyped,
        (config) => getUnifiedFrameworkDataControllerFromConfiguration(keyTyped, config),
        value
      );
    }

    // Prepopulation for frameworks of the framework-registry
    for (const framework of getAllPublicFrameworkIdentifiers()) {
      const dataTypeInPascalCase = convertKebabCaseToPascalCase(framework);
      registerFrameworkFakeFixtureUpload(
        framework as DataTypeEnum,
        (config) => getBasePublicFrameworkDefinition(framework)!.getPublicFrameworkApiClient(config),
        `CompanyInformationWith${dataTypeInPascalCase}Data`.replace('-', '')
      );
    }

    uploadVsmeFixtures(2, 10);
  }
);
