import ViewFrameworkBase from '@/components/generics/ViewFrameworkBase.vue';
import * as path from 'path';
import {
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
  type LksgData,
  LksgDataControllerApi,
  type StoredCompany,
} from '@clients/backend';
import { admin_name, admin_pw } from '@e2e/utils/Cypress.ts';
import { getKeycloakToken } from '@e2e/utils/Auth.ts';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload.ts';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload.ts';
import LksgBaseFrameworkDefinition from '@/frameworks/lksg/BaseFrameworkDefinition';
import { submitButton } from '@sharedUtils/components/SubmitButton';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';

//post company
//post dataset for company

//save relevant data
//go to (https://dataland.com/companies/${companyId}/frameworks/${dataType}
//start donwload journey

describe('As a user, I want to be able to download datasets from Dataland', () => {
  let dataId = '';
  let companyId = '';
  const reportingPeriod = '2021';
  const dataType = DataTypeEnum.Lksg;
  let lksgFixtureWithNoNullFields: FixtureData<LksgData>;

  beforeEach(() => {
    cy.fixture('CompanyInformationWithLksgPreparedFixtures').then(function (jsonContent) {
      const preparedFixturesLksg = jsonContent as Array<FixtureData<LksgData>>;
      lksgFixtureWithNoNullFields = getPreparedFixture('lksg-all-fields', preparedFixturesLksg);
    });
    Cypress.env('excludeBypassQaIntercept', true);

    cy.ensureLoggedIn(admin_name, admin_pw);
    const uniqueCompanyMarker = Date.now().toString();
    const testCompanyName = 'Company-Created-For-Download-Test-' + uniqueCompanyMarker;

    getKeycloakToken(admin_name, admin_pw).then((token) => {
      uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
        companyId = storedCompany.companyId;
        return uploadFrameworkDataForPublicToolboxFramework(
          LksgBaseFrameworkDefinition,
          token,
          companyId,
          reportingPeriod,
          lksgFixtureWithNoNullFields.t
        ).then((dataMetaInformation) => {
          dataId = dataMetaInformation.dataId;
        });
      });
    });

    afterEach(() => {
      Cypress.env('excludeBypassQaIntercept', false);
    });

    it('should download data as csv file, check for appropriate size and delete it afterwards', () => {
      const fileFormat = 'csv';
      const fileName = `${dataId}.${fileFormat}`;

      //get to the right page as user (https://dataland.com/companies/${existingCompanyId}/frameworks/${existingDataType}
      /*  cy.mountWithPlugins(ViewFrameworkBase, {
          props: {
            dataType: dataType,
            companyId: companyId,
          }
        })*/

      cy.spy(ViewFrameworkBase.prototype, 'handleDatasetDownload').as('handleDownload');
      cy.spy(ViewFrameworkBase.prototype, 'forceFileDownload').as('forceDownload');

      cy.get('button[data-test=downloadDataButton]').should('exist').click();
      cy.get('[data-test=downloadModal]')
        .should('exist')
        .within(() => {
          cy.get('[data-test="reportingYearSelector"]').select(reportingPeriod);
          cy.get('[data-test="formatSelector"]').select(fileFormat);
          cy.get('button[data-test=downloadDataButtonInModal]').click();
        });

      cy.get('@handleDownload').should('have.been.called');
      cy.get('@forceDownload').should('have.been.called');

      const filePath = path.join(Cypress.config('downloadsFolder'), fileName);
      cy.readFile(filePath, { timeout: 10000 }).should('exist');

      cy.task('getFileSize', filePath).then((size) => {
        const minimumFileSize = 5000; // in bytes
        expect(size).to.be.greaterThan(minimumFileSize);
      });

      cy.task('deleteFile', filePath).then(() => {
        cy.readFile(filePath).should('not.exist');
      });
    });

    /**
     * Defines interceps and submits data on the lksg upload for the lksg blanket test
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
});

/*
für json check

    const termToCheck = '[object Object]'
    cy.task('checkFileContent', {path: filePath, term: termToCheck}).then((isFound) => {
      expect(isFound).to.be.false;
    })

 */
