import { admin_name, admin_pw, getBaseUrl, uploader_name, uploader_pw } from '@e2e/utils/Cypress';
import { generateDummyCompanyInformation, uploadCompanyViaApi, uploadCompanyViaForm } from '@e2e/utils/CompanyUpload';
import { getKeycloakToken } from '@e2e/utils/Auth';
import {
  IdentifierType,
  DataTypeEnum,
  type EutaxonomyFinancialsData,
  type LksgData,
  type StoredCompany,
} from '@clients/backend';
import { verifySearchResultTableExists } from '@sharedUtils/ElementChecks';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { describeIf } from '@e2e/support/TestUtility';
import { generateReportingPeriod } from '@e2e/fixtures/common//ReportingPeriodFixtures';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload';
import LksgBaseFrameworkDefinition from '@/frameworks/lksg/BaseFrameworkDefinition';
import EuTaxonomyFinancialsBaseFrameworkDefinition from '@/frameworks/eutaxonomy-financials/BaseFrameworkDefinition';

describe('As a user, I expect the dataset upload process to behave as I expect', function () {
  describeIf(
    '',
    {
      executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
    },
    () => {
      const uniqueCompanyMarkerA = Date.now().toString() + 'AAA';
      const uniqueCompanyMarkerB = Date.now().toString() + 'BBB';
      const testCompanyNameForApiUpload =
        'Api-Created-Company-For-UploadFrameworkDataJourneyTest-' + uniqueCompanyMarkerA;
      const testCompanyNameForFormUpload =
        'Form-Created-Company-For-UploadFrameworkDataJourneyTest-' + uniqueCompanyMarkerB;

      const uniqueCompanyMarkerC = Date.now().toString() + 'CCC';
      const testCompanyNameForManyDatasetsCompany =
        'Api-Created-Company-With-Many-FrameworkDatasets' + uniqueCompanyMarkerC;
      let dataIdOfEuTaxoFinancialsUploadForMostRecentPeriod: string;
      let dataIdOfSecondEuTaxoFinancialsUpload: string;
      let dataIdOfLksgUpload: string;
      let storedCompanyForManyDatasetsCompany: StoredCompany;

      before(function uploadOneCompanyWithoutDataAndOneCompanyWithManyDatasets() {
        let euTaxoFinancialPreparedFixture: FixtureData<EutaxonomyFinancialsData>;
        let lksgPreparedFixture: FixtureData<LksgData>;
        cy.fixture('CompanyInformationWithEuTaxonomyFinancialsPreparedFixtures').then(function (jsonContent) {
          const euTaxoFinancialPreparedFixtures = jsonContent as Array<FixtureData<EutaxonomyFinancialsData>>;
          euTaxoFinancialPreparedFixture = getPreparedFixture(
            'lighweight-eu-taxo-financials-dataset',
            euTaxoFinancialPreparedFixtures
          );
        });
        cy.fixture('CompanyInformationWithLksgPreparedFixtures').then(function (jsonContent) {
          const lksgPreparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
          lksgPreparedFixture = getPreparedFixture('LkSG-date-2022-07-30', lksgPreparedFixtures);
        });
        getKeycloakToken(admin_name, admin_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyNameForApiUpload))
            .then(() => {
              return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyNameForManyDatasetsCompany));
            })
            .then((storedCompany) => {
              storedCompanyForManyDatasetsCompany = storedCompany;
              return uploadFrameworkDataForPublicToolboxFramework(
                EuTaxonomyFinancialsBaseFrameworkDefinition,
                token,
                storedCompanyForManyDatasetsCompany.companyId,
                '2023',
                euTaxoFinancialPreparedFixture.t
              );
            })
            .then((dataMetaInformationOfFirstUpload) => {
              dataIdOfEuTaxoFinancialsUploadForMostRecentPeriod = dataMetaInformationOfFirstUpload.dataId;
              const timeDelayInMillisecondsBeforeNextUploadToAssureDifferentTimestamps = 1;
              // eslint-disable-next-line cypress/no-unnecessary-waiting
              return cy
                .wait(timeDelayInMillisecondsBeforeNextUploadToAssureDifferentTimestamps)
                .then(() => {
                  return uploadFrameworkDataForPublicToolboxFramework(
                    EuTaxonomyFinancialsBaseFrameworkDefinition,
                    token,
                    storedCompanyForManyDatasetsCompany.companyId,
                    '2022',
                    euTaxoFinancialPreparedFixture.t
                  );
                })
                .then((dataMetaInformationOfSecondUpload) => {
                  dataIdOfSecondEuTaxoFinancialsUpload = dataMetaInformationOfSecondUpload.dataId;
                  return uploadFrameworkDataForPublicToolboxFramework(
                    LksgBaseFrameworkDefinition,
                    token,
                    storedCompanyForManyDatasetsCompany.companyId,
                    generateReportingPeriod(),
                    lksgPreparedFixture.t
                  );
                })
                .then((dataMetaInformationLksgUpload) => {
                  dataIdOfLksgUpload = dataMetaInformationLksgUpload.dataId;
                });
            });
        });
      });

      it('Go through the whole dataset creation process for a newly created company and verify pages and elements', function () {
        cy.ensureLoggedIn(admin_name, admin_pw);
        cy.visitAndCheckAppMount('/companies');
        verifySearchResultTableExists();

        cy.get('button').contains('New Dataset').click({ force: true });
        cy.url().should('eq', getBaseUrl() + '/companies/choose');
        cy.get('div[id=option1Container').find('a:contains(Add it)').click({ force: true });
        cy.intercept('**/api/metadata*').as('retrieveExistingDatasetsForCompany');
        uploadCompanyViaForm(testCompanyNameForFormUpload).then((company) => {
          cy.wait('@retrieveExistingDatasetsForCompany', { timeout: Cypress.env('medium_timeout_in_ms') as number });
          cy.url().should('eq', getBaseUrl() + '/companies/' + company.companyId + '/frameworks/upload');
        });
      });

      it('Check that the error message is correctly displayed if a PermId is typed in that was already stored in dataland', function () {
        cy.ensureLoggedIn(admin_name, admin_pw);
        cy.visitAndCheckAppMount('/companies/choose');
        const identifierDoesExistMessage = 'There already exists a company with this ID';
        cy.contains(identifierDoesExistMessage).should('not.exist');
        cy.get('button[name="addCompany"]').click();
        cy.get('li[id="createCompanyForm-incomplete"]').should('exist');
        cy.get("input[name='permId']").type(
          assertDefined(storedCompanyForManyDatasetsCompany.companyInformation.identifiers[IdentifierType.PermId])[0]
        );
        cy.contains(identifierDoesExistMessage).should('exist');
        cy.get("input[name='permId']").type('thisshouldnotexist');
        cy.contains(identifierDoesExistMessage).should('not.exist');
      });

      /**
       * Checks if on the "ChoosingFrameworkForDataUpload"-page the expected texts and buttons are displayed based on the
       * uploaded company having two Eu-Taxo-Financials datasets and one LkSG dataset uploaded for it.
       * @param uploadedTestCompanyName bears the company name of the prior uploaded company so that it can be checked
       * if the company name appears as title
       */
      function verifyChoosingFrameworkPageForUploadedTestCompanyWithManyDatasets(
        uploadedTestCompanyName: string
      ): void {
        cy.contains('h1', uploadedTestCompanyName);

        cy.get('div[id=eutaxonomyDataSetsContainer]').contains('Be the first to create this dataset');
        cy.get('div[id=eutaxonomyDataSetsContainer]').contains('Create another dataset for EU Taxonomy Financials');
        cy.get('div[id=eutaxonomyDataSetsContainer]')
          .find("[data-test='createDatasetButton']")
          .should('have.length', 2);

        cy.get('div[id=lksgContainer]').contains('Create another dataset for LkSG');
        cy.get('div[id=lksgContainer]').find('button.p-disabled[aria-label="Create Dataset"]').should('not.exist');
        cy.get('div[id=lksgContainer]').find('button.p-button[aria-label="Create Dataset"]').should('exist');
      }

      /**
       * Checks if on the "ChoosingFrameworkForDataUpload"-page the links for the already existing datasets lead to
       * the correct framework-view-pages.
       * For the Eu-Taxo-Financials datasets it expects the correct data IDs to be attached to the url as query params
       * and for the LkSG dataset it expects no query param to be attached to the url since for LkSG all datasets can
       * be viewed on one single framework-view-page.
       * @param storedCompanyForTest the prior uploaded stored company which bears the company ID and the company name
       * that should be used in the cypress tests
       * @param dataIdOfFirstUploadedEuTaxoFinancialsDataset the data ID of the Eu-Taxo-Financial dataset that was
       * uploaded first in the before-function
       * @param dataIdOfSecondUploadedEuTaxoFinancialsDataset the data ID of the Eu-Taxo-Financial dataset that was
       * uploaded second in the before-function
       * @param dataIdOfLksgDataset the data ID of the Lksg dataset that was
       * uploaded in the before-function
       */
      function checkIfLinksToExistingDatasetsWorkAsExpected(
        storedCompanyForTest: StoredCompany,
        dataIdOfFirstUploadedEuTaxoFinancialsDataset: string,
        dataIdOfSecondUploadedEuTaxoFinancialsDataset: string,
        dataIdOfLksgDataset: string
      ): void {
        cy.get('div[id=eutaxonomyDataSetsContainer')
          .find(`a.text-primary:contains(EU Taxonomy Financials)`)
          .eq(0)
          .click({ force: true });
        cy.contains('h1', storedCompanyForTest.companyInformation.companyName)
          .url()
          .should(
            'eq',
            getBaseUrl() +
              `/companies/${storedCompanyForTest.companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}/${dataIdOfFirstUploadedEuTaxoFinancialsDataset}`
          );
        cy.go('back');
        cy.get('div[id=eutaxonomyDataSetsContainer')
          .find(`a.text-primary:contains(EU Taxonomy Financials)`)
          .eq(1)
          .click({ force: true });
        cy.contains('h1', storedCompanyForTest.companyInformation.companyName)
          .url()
          .should(
            'eq',
            getBaseUrl() +
              `/companies/${storedCompanyForTest.companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}/${dataIdOfSecondUploadedEuTaxoFinancialsDataset}`
          );
        cy.go('back');
        cy.get('div[id=lksgContainer').find(`a.text-primary:contains(LkSG)`).click({ force: true });
        cy.contains('h1', storedCompanyForTest.companyInformation.companyName)
          .url()
          .should(
            'contain',
            `/companies/${storedCompanyForTest.companyId}/frameworks/${DataTypeEnum.Lksg}/${dataIdOfLksgDataset}`
          );
      }

      /**
       * Checks if dataset of a framework is rendered on the "ViewFrameworkData"-page after using the dropdown
       * to select a framework. In the test, it is expected that the change from Eu-Taxo-Financials to LkSG and
       * vice versa renders the dataset".
       */
      function checkIfDropDownSwitchRendersData(): void {
        cy.get('div[data-test="chooseFrameworkDropdown"]').click();
        cy.get("li:contains('EU Taxonomy Financials')").click();
        cy.get('td[data-cell-label="Fiscal Year End"]').should('be.visible');

        cy.get('div[data-test="chooseFrameworkDropdown"]').click();
        cy.get("li:contains('LkSG')").click();
        cy.get('td[data-cell-label="Data Date"]').should('be.visible');

        cy.get('td[data-cell-label="Data Date"]').next('td').find('span').should('be.visible').contains('2022-07-30');
      }

      it(
        'Go through the whole dataset creation process for an existing company, which already has framework data for multiple frameworks,' +
          ' and verify pages and elements.',
        function () {
          cy.ensureLoggedIn(uploader_name, uploader_pw);
          cy.visitAndCheckAppMount('/companies');
          verifySearchResultTableExists();
          cy.get('button').contains('New Dataset').click({ force: true });
          cy.get('input[id=company_search_bar_standard]')
            .should('exist')
            .url()
            .should('eq', getBaseUrl() + '/companies/choose');
          cy.intercept('**/api/companies/names*').as('searchCompanyName');
          cy.get('input[id=company_search_bar_standard]').click({ force: true });
          cy.get('input[id=company_search_bar_standard]').type(testCompanyNameForManyDatasetsCompany);
          cy.wait('@searchCompanyName', { timeout: Cypress.env('short_timeout_in_ms') as number });
          cy.get('ul[class=p-autocomplete-items]').should('exist');
          cy.get('input[id=company_search_bar_standard]').type('{downArrow}');
          cy.intercept('**/api/metadata*').as('retrieveExistingDatasetsForCompany');
          cy.get('input[id=company_search_bar_standard]').type('{enter}');
          cy.wait('@retrieveExistingDatasetsForCompany', { timeout: Cypress.env('short_timeout_in_ms') as number });
          cy.url().should(
            'eq',
            getBaseUrl() + `/companies/${storedCompanyForManyDatasetsCompany.companyId}/frameworks/upload`
          );

          verifyChoosingFrameworkPageForUploadedTestCompanyWithManyDatasets(testCompanyNameForManyDatasetsCompany);

          checkIfLinksToExistingDatasetsWorkAsExpected(
            storedCompanyForManyDatasetsCompany,
            dataIdOfEuTaxoFinancialsUploadForMostRecentPeriod,
            dataIdOfSecondEuTaxoFinancialsUpload,
            dataIdOfLksgUpload
          );

          checkIfDropDownSwitchRendersData();
        }
      );
    }
  );
});
