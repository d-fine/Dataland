import { DataTypeEnum, type SfdrData, type StoredCompany } from '@clients/backend';
import { admin_name, admin_pw, getBaseUrl } from '@e2e/utils/Cypress.ts';
import { getKeycloakToken } from '@e2e/utils/Auth.ts';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload.ts';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload.ts';
import SfdrBaseFrameworkDefinition from '@/frameworks/sfdr/BaseFrameworkDefinition';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { describeIf } from '@e2e/support/TestUtility.ts';

describeIf(
  'As a user, I want to be able edit data points on dataland',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    const reportingPeriod = '2021';
    const dataType = DataTypeEnum.Sfdr;
    let storedCompany: StoredCompany;
    let SfdrFixtureWithNoNullFields: FixtureData<SfdrData>;


    before(() => {
      cy.fixture('CompanyInformationWithSfdrPreparedFixtures').then((jsonContent) => {
        const preparedFixturesSfdr = jsonContent as Array<FixtureData<SfdrData>>;
        SfdrFixtureWithNoNullFields = getPreparedFixture('Sfdr-dataset-with-no-null-fields', preparedFixturesSfdr);
      });

      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        const uniqueCompanyMarker = Date.now().toString();
        const testStoredCompanyName = 'Company-Created-For-Download-Test-' + uniqueCompanyMarker;
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

    it('should open EditDataPointDialog for a BigDecimalExtendedDataPointFormField modal and display its parts', () => {
      cy.visit(getBaseUrl() + `/companies/${storedCompany.companyId}/frameworks/${dataType}`);
      cy.get('button[data-test=editDatasetButton]').should('exist').click();
        cy.contains('span.table-left-label', 'Scope 1 GHG emissions')
            .closest('td')
            .next('td')
            .find('button[data-test="edit-data-point-button"]')
            .click();
    });
  }
);
