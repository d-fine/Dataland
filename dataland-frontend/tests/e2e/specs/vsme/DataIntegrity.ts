import { describeIf } from '@e2e/support/TestUtility';
import { admin_name, admin_pw, admin_userId, getBaseUrl } from '@e2e/utils/Cypress';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { type DataMetaInformation, DataTypeEnum, type StoredCompany } from '@clients/backend';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { submitButton } from '@sharedUtils/components/SubmitButton';
import * as MLDT from '@sharedUtils/components/resources/dataTable/MultiLayerDataTableTestUtils';
import { UploadReports } from '@sharedUtils/components/UploadReports';
import { TEST_PDF_FILE_NAME, TEST_PRIVATE_PDF_FILE_PATH } from '@sharedUtils/ConstantsForPdfs';
import { assignCompanyRole } from '@e2e/utils/CompanyRolesUtils';
import { CompanyRole } from '@clients/communitymanager';

let tokenForAdminUser: string;
let storedTestCompany: StoredCompany;
const uploadReports = new UploadReports('referencedReports');
describeIf(
  'As a user, I expect to be able to edit and submit Vsme data via the upload form',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
    onlyExecuteWhenEurodatIsLive: true,
  },
  function (): void {
    beforeEach(() => {
      const uniqueCompanyMarker = Date.now().toString();
      const testCompanyName = 'Company-Created-In-Vsme-Blanket-Test-' + uniqueCompanyMarker;

      getKeycloakToken(admin_name, admin_pw)
        .then((token: string) => {
          tokenForAdminUser = token;

          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName));
        })
        .then((storedCompany) => {
          storedTestCompany = storedCompany;
          void assignCompanyRole(
            tokenForAdminUser,
            CompanyRole.CompanyOwner,
            storedTestCompany.companyId,
            admin_userId
          );
        });
    });

    /**
     * Fills out an AddressFormField
     * @param inputSection the section to which the AddressFormField belongs to
     * @param inputAdressFormField the actual name of the data-test marker of the respective AddressFormField
     */
    function fillOutAdressFormField(inputSection: string, inputAdressFormField: string): void {
      cy.get(`[data-test=${inputSection}]`)
        .find(`[data-test=${inputAdressFormField}]`)
        .find('[name="streetAndHouseNumber"]')
        .type('Test-Address');
      cy.get(`[data-test=${inputSection}]`)
        .find(`[data-test=${inputAdressFormField}]`)
        .find('[data-test="country"]')
        .click();
      cy.get('ul.p-dropdown-items li').contains(`Afghanistan`).click();
      cy.get(`[data-test=${inputSection}]`)
        .find(`[data-test=${inputAdressFormField}]`)
        .find('[name="city"]')
        .type('Test-City');
      cy.get(`[data-test=${inputSection}]`)
        .find(`[data-test=${inputAdressFormField}]`)
        .find('[name="postalCode"]')
        .type('12345');
    }

    /**
     * Fill out the vsme subsidiary section
     */
    function fillOutSubsidiarySection(): void {
      cy.get('[data-test="addNewSubsidiaryButton"]').click();
      cy.get('[data-test="subsidiarySection"]').should('exist');
      cy.get('[data-test="subsidiarySection"]').get('[name="nameOfSubsidiary"]').type('Test-Subsidiary');
      fillOutAdressFormField('subsidiarySection', 'AddressFormField');
    }
    /**
     * Fill out the vsme pollution emission section
     */
    function fillOutPollutionEmissionSection(): void {
      cy.get('[data-test="PollutionEmissionSection"]').should('exist');
      cy.get('[data-test="PollutionEmissionSection"]').get('[name="pollutionType"]').type('Test-Waste-Type');
      cy.get('[data-test="PollutionEmissionSection"]').get('[name="emissionInKilograms"]').type('12345');
      cy.get('[data-test="PollutionEmissionSection"]').find('[data-test="relaseMedium"]').click();
      cy.get('ul.p-dropdown-items li').contains(`Air`).click();
    }
    /**
     * Fill out the vsme site and area section
     */
    function fillOutSiteAndAreaSection(): void {
      cy.get('[data-test="SiteAndAreaSection"]').should('exist');
      cy.get('[data-test="SiteAndAreaSection"]').get('[name="siteName"]').type('Test-Site-Name');
      fillOutAdressFormField('SiteAndAreaSection', 'AddressFormFieldSite');
      cy.get('[data-test="SiteAndAreaSection"]').find('[name="siteGeocoordinateLongitudeval"]').type('12345');
      cy.get('[data-test="SiteAndAreaSection"]').find('[name="siteGeocoordinateLatitude"]').type('12345');
      cy.get('[data-test="SiteAndAreaSection"]')
        .get('[name="biodiversitySensitiveArea"]')
        .type('Test-Site-Biodiversity-Area');
      fillOutAdressFormField('SiteAndAreaSection', 'AddressFormFieldArea');
      cy.get('[data-test="SiteAndAreaSection"]').find('[name="areaInHectare"]').type('12345');
      cy.get('[data-test="SiteAndAreaSection"]').find('[name="areaGeocoordinateLatitude"]').type('12345');
      cy.get('[data-test="SiteAndAreaSection"]').find('[name="areaGeocoordinateLongitude"]').type('12345');
      cy.get('[data-test="SiteAndAreaSection"]').find('[data-test="specificationOfAdjointness"]').click();
      cy.get('ul.p-dropdown-items li').contains(`In`).click();
    }
    /**
     * Fill out the vsme waste classification section
     */
    function fillOutWasteClassificationSection(): void {
      cy.get('[data-test="WasteClassificationSection"]').should('exist');
      cy.get('[data-test="WasteClassificationSection"]').find('[data-test="wasteClassification"]').click();
      cy.get('ul.p-dropdown-items li').contains(`Hazardous`).click();
      cy.get('[data-test="WasteClassificationSection"]').find('[name="typeOfWaste"]').type('Test-Waste');
      cy.get('[data-test="WasteClassificationSection"]').find('[name="totalAmountOfWasteInTonnes"]').type('12345');
      cy.get('[data-test="WasteClassificationSection"]').find('[name="wasteRecycleOrReuseInTonnes"]').type('12345');
      cy.get('[data-test="WasteClassificationSection"]').find('[name="wasteDisposalInTonnes"]').type('12345');
      cy.get('[data-test="WasteClassificationSection"]').find('[name="totalAmountOfWasteInCubicMeters"]').type('12345');
      cy.get('[data-test="WasteClassificationSection"]')
        .find('[name="wasteRecycleOrReuseInCubicMeters"]')
        .type('12345');
      cy.get('[data-test="WasteClassificationSection"]').find('[name="wasteDisposalInCubicMeters"]').type('12345');
    }
    /**
     * Fill out the vsme employees per country section
     */
    function fillOutEmployeesPerCountrySection(): void {
      cy.get('[data-test="addNewEmployeesPerCountryButton"]').click();
      cy.get('[data-test="employeesPerCountrySection"]').should('exist');
      cy.get('[data-test="employeesPerCountrySection"]').find('[data-test="country"]').click();
      cy.get('ul.p-dropdown-items li').contains(`Afghanistan`).click();
      cy.get('[data-test="employeesPerCountrySection"]').find('[name="numberOfEmployeesInHeadCount"]').type('12345');
      cy.get('[data-test="employeesPerCountrySection"]')
        .find('[name="numberOfEmployeesInFullTimeEquivalent"]')
        .type('12345');
    }
    /**
     * Fill out a datapoint with an attached document
     */
    function fillOutOneDatePointWithAttachedDocument(): void {
      cy.get('[data-test="electricityTotalInMWh"]')
        .find('div[data-test="toggleDataPointWrapper"]')
        .find('div[data-test="dataPointToggleButton"]')
        .click();
      cy.get('[data-test="electricityTotalInMWh"]').find('div[data-test="value"]').find('[name="value"]').type('12345');
      cy.get('[data-test="electricityTotalInMWh"]').find('div[name="quality"]').click();
      cy.get('ul.p-dropdown-items li').contains(`Audited`).click();
      cy.get('[data-test="electricityTotalInMWh"]').find('div[name="fileName"]').click();
      cy.get('ul.p-dropdown-items li').contains(`${TEST_PDF_FILE_NAME}-private`).click();
    }
    /**
     * Upload a document and verify that it worked
     */
    function uploadDocument(): void {
      uploadReports.selectFile(`${TEST_PDF_FILE_NAME}-private`);
      uploadReports.validateReportToUploadHasContainerInTheFileSelector(`${TEST_PDF_FILE_NAME}-private`);
      uploadReports.validateReportToUploadHasContainerWithInfoForm(`${TEST_PDF_FILE_NAME}-private`);
    }

    /**
     * Check that data can be viewed and documents downloaded
     */
    function verifyDocumentDownloadAndDataIsViewable(): void {
      cy.wait('@waitOnMyDatasetPage', { timeout: Cypress.env('medium_timeout_in_ms') as number });
      cy.wait('@postCompanyAssociatedData', { timeout: Cypress.env('medium_timeout_in_ms') as number }).then(
        (postResponseInterception) => {
          cy.url().should('eq', getBaseUrl() + '/datasets');
          const dataMetaInformationOfReuploadedDataset = postResponseInterception.response?.body as DataMetaInformation;

          cy.visitAndCheckAppMount(
            '/companies/' +
              storedTestCompany.companyId +
              '/frameworks/' +
              DataTypeEnum.Vsme +
              '/' +
              dataMetaInformationOfReuploadedDataset.dataId
          );

          MLDT.getSectionHead('Energy and greenhous gas emissions').should(
            'have.attr',
            'data-section-expanded',
            'true'
          );
          MLDT.getCellValueContainer('Electricity Total').find('a.link').should('include.text', 'MWh').click();
          const expectedPathToDownloadedReport =
            Cypress.config('downloadsFolder') + `/${TEST_PDF_FILE_NAME}-private.pdf`;
          cy.readFile(expectedPathToDownloadedReport).should('not.exist');
          cy.intercept('**/api/data/' + DataTypeEnum.Vsme + '/documents*').as('documentDownload');
          cy.get('[data-test="Report-Download-some-document-private"]').click();
          // eslint-disable-next-line cypress/no-unnecessary-waiting
          cy.wait(500);
          cy.wait('@documentDownload');
          cy.readFile(`../${TEST_PRIVATE_PDF_FILE_PATH}`, 'binary', {
            timeout: Cypress.env('medium_timeout_in_ms') as number,
          }).then((expectedFileBinary) => {
            cy.task('calculateHash', expectedFileBinary).then((expectedFileHash) => {
              cy.readFile(expectedPathToDownloadedReport, 'binary', {
                timeout: Cypress.env('medium_timeout_in_ms') as number,
              }).then((receivedFileHash) => {
                cy.task('calculateHash', receivedFileHash).should('eq', expectedFileHash);
              });
              cy.task('deleteFolder', Cypress.config('downloadsFolder'));
            });
          });
        }
      );
    }

    it('Create a company and a Vsme dataset via api, then assure that the dataset equals the pre-uploaded one', () => {
      cy.ensureLoggedIn(admin_name, admin_pw);
      cy.intercept('**/api/companies/' + storedTestCompany.companyId + '/info').as('getCompanyInformation');
      cy.visitAndCheckAppMount(
        '/companies/' + storedTestCompany.companyId + '/frameworks/' + DataTypeEnum.Vsme + '/upload'
      );
      cy.get('h1').should('contain', storedTestCompany.companyInformation.companyName);
      cy.intercept({
        url: `**/api/data/${DataTypeEnum.Vsme}`,
        times: 1,
      }).as('postCompanyAssociatedData');
      cy.get('[data-test="reportingPeriod"]').click();
      cy.get('div.p-datepicker').get('div.p-yearpicker').click();
      fillOutSubsidiarySection();
      fillOutPollutionEmissionSection();
      fillOutSiteAndAreaSection();
      fillOutWasteClassificationSection();
      fillOutEmployeesPerCountrySection();
      uploadDocument();

      fillOutOneDatePointWithAttachedDocument();
      cy.intercept({
        url: `**/api/data/${DataTypeEnum.Vsme}`,
        times: 1,
      }).as('postCompanyAssociatedData');
      cy.intercept('**/api/users/**').as('waitOnMyDatasetPage');
      submitButton.clickButton();
      verifyDocumentDownloadAndDataIsViewable();
    });
  }
);
