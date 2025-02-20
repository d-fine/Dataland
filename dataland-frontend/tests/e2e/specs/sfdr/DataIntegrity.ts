import { describeIf } from '@e2e/support/TestUtility';
import { admin_name, admin_pw, getBaseUrl } from '@e2e/utils/Cypress';
import { DataTypeEnum, type SfdrData } from '@clients/backend';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { generateDummyCompanyInformation } from '@e2e/utils/CompanyUpload';
import { selectSingleReportAndFillWithData } from '@e2e/utils/UploadUtils';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { submitButton } from '@sharedUtils/components/SubmitButton';
import * as MLDT from '@sharedUtils/components/resources/dataTable/MultiLayerDataTableTestUtils';
import { uploadCompanyAndFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload';
import { TEST_PDF_FILE_NAME } from '@sharedUtils/ConstantsForPdfs';
import { type ObjectType } from '@/utils/UpdateObjectUtils';
import { type ExtendedDataPoint } from '@/utils/DataPoint';
import { selectItemFromDropdownByIndex, selectItemFromDropdownByValue } from '@sharedUtils/Dropdown';
import SfdrBaseFrameworkDefinition from '@/frameworks/sfdr/BaseFrameworkDefinition';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';

let testSfdrCompany: FixtureData<SfdrData>;
before(function () {
  cy.fixture('CompanyInformationWithSfdrPreparedFixtures').then(function (jsonContent) {
    const sfdrPreparedFixtures = jsonContent as Array<FixtureData<SfdrData>>;
    testSfdrCompany = getPreparedFixture('Sfdr-dataset-with-no-null-fields', sfdrPreparedFixtures);
  });
});
describeIf(
  'As a user, I expect that the upload form works correctly when editing and uploading a new SFDR dataset',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function () {
    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
    });

    /**
     * validates that the data uploaded via the function `uploadSfdrDataViaForm` is displayed correctly for a company
     * @param companyId the company associated to the data uploaded via form
     */
    function validateFormUploadedData(companyId: string): void {
      cy.visit('/companies/' + companyId + '/frameworks/' + DataTypeEnum.Sfdr);

      MLDT.getSectionHead('Environmental').should('have.attr', 'data-section-expanded', 'true');
      MLDT.getSectionHead('Biodiversity').should('have.attr', 'data-section-expanded', 'true');
      MLDT.getSectionHead('Energy performance').should('have.attr', 'data-section-expanded', 'true');

      MLDT.getCellValueContainer('Primary Forest And Wooded Land Of Native Species Exposure').should(
        'contain.text',
        'Yes'
      );
      MLDT.getCellValueContainer('Protected Areas Exposure').should('contain.text', 'No');
      MLDT.getCellValueContainer('Rare Or Endangered Ecosystems Exposure').should('contain.text', 'Yes');
      MLDT.getCellValueContainer('Applicable High Impact Climate Sectors')
        .find('a.link')
        .should('contain.text', 'Applicable High Impact Climate Sectors')
        .click();

      cy.get('div.p-dialog-header').should('contain.text', 'Applicable High Impact Climate Sectors');
    }

    /**
     * Set reference to all uploaded reports while pushing a new one as well
     * @param referencedReports all reports already uploaded
     */
    function setReferenceToAllUploadedReports(referencedReports: string[]): void {
      referencedReports.forEach((it, index) => {
        selectHighImpactClimateSectorAndReport(index, it);
      });
    }

    /**
     * Selects a high impact climate sector from the dropdown and assigns a reference to it
     * @param sectorCardIndex The index of the sector card to which the reference should be assigned
     * @param reportToReference The name of the report to reference
     */
    function selectHighImpactClimateSectorAndReport(sectorCardIndex: number, reportToReference: string): void {
      cy.get('div[data-test="applicableHighImpactClimateSectors"]').find('div.p-multiselect-trigger').click();
      cy.get('li.p-multiselect-item')
        .eq(sectorCardIndex)
        .invoke('attr', 'aria-selected')
        .then((ariaSelected) => {
          if (ariaSelected === 'false') {
            cy.get('li.p-multiselect-item').eq(sectorCardIndex).click();
          }
        });
      selectItemFromDropdownByValue(
        cy.get('div[data-test="applicableHighImpactClimateSector"]').find('div[name="fileName"]').eq(sectorCardIndex),
        reportToReference
      );
    }

    /**
     * Check if YesNoExtendedDataPointFormField component contain proper fields
     * @param fieldData Data for field
     */
    function testYesNoExtendedDataPointFormField(fieldData: ExtendedDataPoint<string>): void {
      cy.get('[data-test="protectedAreasExposure"]')
        .find('div[data-test="dataQuality"] span.p-dropdown-label')
        .should('exist')
        .should('contain.text', humanizeStringOrNumber(fieldData.quality));
      cy.get('[data-test="protectedAreasExposure"] [data-test="toggleDataPointWrapper"] input[value="Yes"]').click();
      cy.get('[data-test="protectedAreasExposure"] [data-test="toggleDataPointWrapper"] input[value="No"]').click();
      cy.get('[data-test="protectedAreasExposure"] div[data-test="dataQuality"] span.p-dropdown-label')
        .should('exist')
        .should('contain.text', humanizeStringOrNumber(fieldData.quality));
      cy.get('[data-test="protectedAreasExposure"] [data-test="toggleDataPointWrapper"] input[value="No"]').click();
      cy.get('[data-test="protectedAreasExposure"] [data-test="toggleDataPointWrapper"] input[value="No"]').click();
      cy.get('[data-test="protectedAreasExposure"] div[data-test="dataQuality"] span.p-dropdown-label')
        .should('exist')
        .should('contain.text', humanizeStringOrNumber(fieldData.quality));
    }

    /**
     * Removes the first high impact climate sector and checks that it has actually disappeared
     */
    function testRemovingOfHighImpactClimateSector(): void {
      cy.get('div[data-test="applicableHighImpactClimateSector"]:contains("A - AGRICULTURE, FORESTRY AND FISHING")')
        .find("em:contains('close')")
        .click();
      cy.get(
        'div[data-test="applicableHighImpactClimateSector"]:contains("A - AGRICULTURE, FORESTRY AND FISHING")'
      ).should('not.exist');
    }

    /**
     * Set the quality for the sfdr test dataset
     */
    function setQualityInSfdrUploadForm(): void {
      selectItemFromDropdownByIndex(
        cy.get('[data-test="primaryForestAndWoodedLandOfNativeSpeciesExposure"]').find('div[name="quality"]'),
        3
      );
      selectItemFromDropdownByIndex(
        cy.get('[data-test="rareOrEndangeredEcosystemsExposure"]').find('div[name="quality"]'),
        3
      );
    }

    it('Create a company and a SFDR dataset via the api, then edit the SFDR dataset and re-upload it via the form', () => {
      const uniqueCompanyMarker = Date.now().toString();
      const companyName = 'Company-Created-In-Sfdr-DataIntegrity-Test-' + uniqueCompanyMarker;
      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadCompanyAndFrameworkDataForPublicToolboxFramework(
          SfdrBaseFrameworkDefinition,
          token,
          generateDummyCompanyInformation(companyName),
          testSfdrCompany.t,
          '2021'
        ).then((uploadIds) => {
          cy.intercept(`**/api/data/${DataTypeEnum.Sfdr}/${uploadIds.dataId}`).as('fetchDataForPrefill');
          cy.visitAndCheckAppMount(
            '/companies/' +
              uploadIds.companyId +
              '/frameworks/' +
              DataTypeEnum.Sfdr +
              '/upload' +
              '?templateDataId=' +
              uploadIds.dataId
          );
          cy.wait('@fetchDataForPrefill', { timeout: Cypress.env('medium_timeout_in_ms') as number });
          cy.get('h1').should('contain', companyName);
          selectSingleReportAndFillWithData();
          setQualityInSfdrUploadForm();
          setReferenceToAllUploadedReports(
            Object.keys(testSfdrCompany.t.general.general.referencedReports as ObjectType)
          );
          testYesNoExtendedDataPointFormField(
            testSfdrCompany.t.environmental?.biodiversity?.protectedAreasExposure as ExtendedDataPoint<string>
          );
          testRemovingOfHighImpactClimateSector();
          submitButton.clickButton();
          cy.get('div.p-message-success:not(.p-message-error)').should('not.contain', 'An unexpected error occurred.');
          cy.url().should('eq', getBaseUrl() + '/datasets');
          cy.get('[data-test="datasets-table"]').should('be.visible');
          validateFormUploadedData(uploadIds.companyId);
        });
      });
    });
  }
);
