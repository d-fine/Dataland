import { join } from 'path';
import { type DataMetaInformation, DataTypeEnum, type LksgData, type StoredCompany } from '@clients/backend';
import { admin_name, admin_pw, getBaseUrl } from '@e2e/utils/Cypress.ts';
import { getKeycloakToken } from '@e2e/utils/Auth.ts';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload.ts';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload.ts';
import LksgBaseFrameworkDefinition from '@/frameworks/lksg/BaseFrameworkDefinition';
import { submitButton } from '@sharedUtils/components/SubmitButton';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { assignCompanyOwnershipToDatalandAdmin } from '@e2e/utils/CompanyRolesUtils.ts';

describe('As a user, I want to be able to download datasets from Dataland', () => {
  const reportingPeriod = '2021';
  const dataType = DataTypeEnum.Lksg;
  const minimumFileSizeInByte = 5000;

  let dataId: string;
  let companyId: string;
  let lksgFixtureWithNoNullFields: FixtureData<LksgData>;

  beforeEach(() => {
    cy.fixture('CompanyInformationWithLksgPreparedFixtures').then((jsonContent) => {
      const preparedFixturesLksg = jsonContent as Array<FixtureData<LksgData>>;
      lksgFixtureWithNoNullFields = getPreparedFixture('lksg-all-fields', preparedFixturesLksg);
    });
    Cypress.env('excludeBypassQaIntercept', true);

    cy.ensureLoggedIn(admin_name, admin_pw);
    const uniqueCompanyMarker = Date.now().toString();
    const testCompanyName = 'Company-Created-For-Download-Test-' + uniqueCompanyMarker;

    getKeycloakToken(admin_name, admin_pw).then((token: string) => {
      return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
        return assignCompanyOwnershipToDatalandAdmin(token, storedCompany.companyId).then(() => {
          companyId = storedCompany.companyId;
          return uploadFrameworkDataForPublicToolboxFramework(
            LksgBaseFrameworkDefinition,
            token,
            storedCompany.companyId,
            '2021',
            lksgFixtureWithNoNullFields.t
          ).then((dataMetaInformation) => {
            dataId = dataMetaInformation.dataId;
            interceptsAndSubmitsDataset(storedCompany, dataMetaInformation, testCompanyName);
          });
        });
      });
    });
  });

  afterEach(() => {
    Cypress.env('excludeBypassQaIntercept', false);
  });

  it('should download data as csv file, check for appropriate size and delete it afterwards', () => {
    const fileFormat = 'csv';
    const fileName = `${dataId}.${fileFormat}`;

    cy.visit(getBaseUrl() + `/companies/${companyId}/frameworks/${dataType}`);

    cy.get('button[data-test=downloadDataButton]').should('exist').click();
    cy.get('[data-test=downloadModal]')
      .should('exist')
      .within(() => {
        cy.get('[data-test="reportingYearSelector"]').select(reportingPeriod);
        cy.get('[data-test="formatSelector"]').select(fileFormat);
        cy.get('button[data-test=downloadDataButtonInModal]').click();
      });

    const filePath = join(Cypress.config('downloadsFolder'), fileName);
    cy.readFile(filePath, { timeout: Cypress.env('short_timeout_in_ms') as number }).should('exist');

    cy.task('getFileSize', filePath).then((size) => {
      expect(size).to.be.greaterThan(minimumFileSizeInByte);
    });

    cy.task('deleteFile', filePath).then(() => {
      cy.readFile(filePath).should('not.exist');
    });
  });

  it('should download data as csv file ment to open in excel, check for appropriate size and delete it afterwards', () => {
    const fileFormat = 'json';
    const fileName = `${dataId}.${fileFormat}`;

    cy.visit(getBaseUrl() + `/companies/${companyId}/frameworks/${dataType}`);

    cy.get('button[data-test=downloadDataButton]').should('exist').click();
    cy.get('[data-test=downloadModal]')
      .should('exist')
      .within(() => {
        cy.get('[data-test="reportingYearSelector"]').select(reportingPeriod);
        cy.get('[data-test="formatSelector"]').select(fileFormat);
        cy.get('button[data-test=downloadDataButtonInModal]').click();
      });

    const filePath = join(Cypress.config('downloadsFolder'), fileName);
    cy.readFile(filePath, { timeout: Cypress.env('short_timeout_in_ms') as number }).should('exist');

    const termToCheck = 'sep=,';
    cy.task('checkFileContent', { path: filePath, term: termToCheck }).then((isFound) => {
      expect(isFound).to.be.true;
    });

    cy.task('getFileSize', filePath).then((size) => {
      expect(size).to.be.greaterThan(minimumFileSizeInByte);
    });

    cy.task('deleteFile', filePath).then(() => {
      cy.readFile(filePath).should('not.exist');
    });
  });

  it('should download data as json file, check for appropriate size and delete it afterwards', () => {
    const fileFormat = 'json';
    const fileName = `${dataId}.${fileFormat}`;

    cy.visit(getBaseUrl() + `/companies/${companyId}/frameworks/${dataType}`);

    cy.get('button[data-test=downloadDataButton]').should('exist').click();
    cy.get('[data-test=downloadModal]')
      .should('exist')
      .within(() => {
        cy.get('[data-test="reportingYearSelector"]').select(reportingPeriod);
        cy.get('[data-test="formatSelector"]').select(fileFormat);
        cy.get('button[data-test=downloadDataButtonInModal]').click();
      });

    const filePath = join(Cypress.config('downloadsFolder'), fileName);
    cy.readFile(filePath, { timeout: Cypress.env('short_timeout_in_ms') as number }).should('exist');

    cy.task('getFileSize', filePath).then((size) => {
      expect(size).to.be.greaterThan(minimumFileSizeInByte);
    });

    cy.task('deleteFile', filePath).then(() => {
      cy.readFile(filePath).should('not.exist');
    });
  });

  /**
   * Defines intercepts and submits data on the LkSG upload for the LkSG blanket test
   * @param storedCompany stored company information
   * @param dataMetaInformation meta data information
   * @param testCompanyName name of the company
   */
  function interceptsAndSubmitsDataset(
    storedCompany: StoredCompany,
    dataMetaInformation: DataMetaInformation,
    testCompanyName: string
  ): void {
    cy.intercept('**/api/companies/' + storedCompany.companyId + '/info').as('getCompanyInformation');
    cy.visitAndCheckAppMount(
      '/companies/' +
        storedCompany.companyId +
        '/frameworks/' +
        DataTypeEnum.Lksg +
        '/upload?templateDataId=' +
        dataMetaInformation.dataId
    );
    cy.wait('@getCompanyInformation', { timeout: Cypress.env('medium_timeout_in_ms') as number });
    cy.get('h1').should('contain', testCompanyName);
    cy.intercept({
      url: `**/api/data/${DataTypeEnum.Lksg}*`,
      times: 1,
    }).as('postCompanyAssociatedData');
    submitButton.clickButton();
  }
});
