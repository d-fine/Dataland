import { DataTypeEnum, type SfdrData, type StoredCompany } from '@clients/backend';
import { admin_name, admin_pw, getBaseUrl } from '@e2e/utils/Cypress.ts';
import { getKeycloakToken } from '@e2e/utils/Auth.ts';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload.ts';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload.ts';
import SfdrBaseFrameworkDefinition from '@/frameworks/sfdr/BaseFrameworkDefinition.ts';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures.ts';
import { describeIf } from '@e2e/support/TestUtility.ts';

const dataType = DataTypeEnum.Sfdr;
let storedCompany: StoredCompany;
/**
 * Navigates to the framework edit mode page for the stored company
 */
function navigateToEditMode(): void {
  cy.visit(getBaseUrl() + `/companies/${storedCompany.companyId}/frameworks/${dataType}`);
  cy.get('button[data-test=editDataPointsButton]').should('exist').click();
}

/**
 * Opens the edit dialog for a specific data field
 * @param dataPointType - The type of data point to edit
 */
function openEditDialog(dataPointType: string): void {
  cy.get(`[data-test="edit-data-point-icon-${dataPointType}"]`).click();
}

/**
 * Saves the current data point and waits for successful response
 */
function saveDataPoint(): void {
  cy.intercept('POST', '**/api/data-points?bypassQa=true').as('saveDataPoint');
  cy.get('[data-test="save-data-point-button"]').should('be.visible').click();
  cy.wait('@saveDataPoint').its('response.statusCode').should('be.oneOf', [200, 201]);
}

/**
 * Verifies that a field contains the expected value in the table view
 * @param fieldLabel - The label of the field to verify
 * @param expectedValue - The expected value to be displayed
 */
function verifyFieldValue(fieldLabel: string, expectedValue: string): void {
  cy.contains('span.table-left-label', fieldLabel).closest('td').next('td').should('contain', expectedValue);
}

describeIf(
  'As a user, I want to be able edit data points on dataland',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    const reportingPeriod = '2021';
    let SfdrFixtureWithNoNullFields: FixtureData<SfdrData>;

    before(() => {
      cy.fixture('CompanyInformationWithSfdrPreparedFixtures').then((jsonContent) => {
        const preparedFixturesSfdr = jsonContent as Array<FixtureData<SfdrData>>;
        SfdrFixtureWithNoNullFields = getPreparedFixture('Sfdr-dataset-with-no-null-fields', preparedFixturesSfdr);
      });

      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        const uniqueCompanyMarker = Date.now().toString();
        const testStoredCompanyName = 'Company-Created-For-EditDataPoint-Test-' + uniqueCompanyMarker;
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(testStoredCompanyName)).then(
          (newStoredCompany) => {
            storedCompany = newStoredCompany;
            return uploadFrameworkDataForPublicToolboxFramework(
              SfdrBaseFrameworkDefinition,
              token,
              storedCompany.companyId,
              reportingPeriod,
              SfdrFixtureWithNoNullFields.t,
              true
            );
          }
        );
      });
    });

    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
    });

    it('should open a BigDecimal EditDataPointDialog, edit all fields and save changes successfully', () => {
      const newValue = '1234.56';

      navigateToEditMode();
      openEditDialog('extendedDecimalScope1GhgEmissionsInTonnes');

      cy.get('div.p-dialog-content')
        .should('be.visible')
        .within(() => {
          cy.get('[data-test="big-decimal-input"] input')
            .should('be.visible')
            .should('have.value', '17,992.73');
        });

      cy.get('[data-test="quality-select"]')
        .should('be.visible')
        .find('.p-select-label, .p-dropdown-label')
        .should('contain', 'Estimated');

      cy.get('[data-test="quality-select"]').click();
      cy.get('[aria-label="Reported"]').click();
      cy.get('[data-test="comment-textarea"]').should('have.value', 'connect haptic program');
      cy.get('div.p-dialog-content').within(() => {
        cy.get('[data-test="big-decimal-input"] input').clear();
        cy.get('[data-test="big-decimal-input"] input').type(newValue);
      });

      cy.get('[data-test="big-decimal-input"] input').blur();
      saveDataPoint();

      cy.get('div.p-dialog-content').should('not.exist');
      cy.contains('span.table-left-label', 'Scope 1 GHG emissions')
        .closest('td')
        .next('td')
        .within(() => {
          cy.get('span[meta-info]').contains('1,234.56 Tonnes');
        });

      cy.contains('span.table-left-label', 'Scope 1 GHG emissions')
        .closest('td')
        .next('td')
        .within(() => {
          cy.get('span[meta-info]').click();
        });

      cy.contains('span.table-left-label', 'Quality').closest('th').next('td').should('contain', 'Reported');
    });

    it('should open a YesNo EditDataPointDialog, edit all fields and save changes successfully', () => {
      navigateToEditMode();
      openEditDialog('extendedEnumYesNoFossilFuelSectorExposure');

      cy.get('div.p-dialog-content')
        .should('be.visible')
        .within(() => {
          cy.get('[data-test="yes-no-select"]').should('be.visible');
          cy.get('[data-test="yes-no-select"]').find('[aria-pressed="true"]').should('contain', 'Yes');
          cy.get('[data-test="yes-no-select"]').contains('No').click();
        });

      saveDataPoint();
      verifyFieldValue('Fossil Fuel Sector Exposure', 'No');
    });

    it('should open a Currency EditDataPointDialog, edit all fields and save changes successfully', () => {
      const newValue = '1234.56';

      navigateToEditMode();
      openEditDialog('extendedCurrencyAverageGrossHourlyEarningsMaleEmployees');

      cy.get('div.p-dialog-content')
        .should('be.visible')
        .within(() => {
          cy.get('[data-test="currency-value-input"] input').should('exist').should('have.value', '1,838,828,082.29');

          cy.get('[data-test="currency"]').should('exist');
        });

      cy.get('[data-test="currency-value-input"] input').clear();
      cy.get('[data-test="currency-value-input"] input').type(newValue);

      cy.get('[data-test="currency-value-input"] input').blur();

      cy.get('[data-test="currency"]').click();
      cy.get('[aria-label="East Caribbean Dollar (XCD)"]').click();

      saveDataPoint();
      verifyFieldValue('Average Gross Hourly Earnings Male Employees', '1,234.56');
    });
  }
);
