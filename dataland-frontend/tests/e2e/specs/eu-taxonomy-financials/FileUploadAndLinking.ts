import { describeIf } from '@e2e/support/TestUtility';
import { checkIfLinkedReportsAreDownloadable, gotoEditForm } from '@e2e/utils/EuTaxonomyFinancialsUpload';
import {
  type CompanyAssociatedDataEutaxonomyFinancialsData,
  DataTypeEnum,
  type EutaxonomyFinancialsData,
} from '@clients/backend';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { admin_name, admin_pw } from '@e2e/utils/Cypress';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { TEST_PDF_FILE_NAME } from '@sharedUtils/ConstantsForPdfs';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload';
import { UploadReports } from '@sharedUtils/components/UploadReports';
import { selectItemFromDropdownByValue } from '@sharedUtils/Dropdown';
import EuTaxonomyFinancialsBaseFrameworkDefinition from '@/frameworks/eutaxonomy-financials/BaseFrameworkDefinition';

describeIf(
  'As a user, I want to add and link documents to the EU Taxonomy form',

  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function () {
    let euTaxoFinancialsFixture: FixtureData<EutaxonomyFinancialsData>;
    const uploadReports = new UploadReports('referencedReports');

    before(function () {
      cy.fixture('CompanyInformationWithEutaxonomyFinancialsPreparedFixtures').then(function (jsonContent) {
        const preparedFixtures = jsonContent as Array<FixtureData<EutaxonomyFinancialsData>>;
        euTaxoFinancialsFixture = getPreparedFixture('lighweight-eu-taxo-financials-dataset', preparedFixtures);
      });
    });

    /**
     * Clicks the report dropdown in a datapoint and selects a report.
     * @param fullLabelOfDatapoint determines the datapoint of which the dropdown will be clicked
     * @param reportName determines the report that will be chosen from the dropdown
     */
    function selectReportForDatapoint(fullLabelOfDatapoint: string, reportName: string): void {
      selectItemFromDropdownByValue(
        cy
          .get('h5')
          .contains(fullLabelOfDatapoint + ' Report')
          .parent()
          .parent()
          .parent()
          .find(`[name="fileName"]`),
        reportName
      );
    }

    it('Check if the files upload works as expected', () => {
      euTaxoFinancialsFixture.companyInformation.companyName =
        'financials-upload-form-document-upload-test' + Date.now();
      let areBothDocumentsStillUploaded = true;
      let storedCompanyId: string;
      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadCompanyViaApi(
          token,
          generateDummyCompanyInformation(euTaxoFinancialsFixture.companyInformation.companyName)
        ).then((storedCompany) => {
          storedCompanyId = storedCompany.companyId;
          return uploadFrameworkDataForPublicToolboxFramework(
            EuTaxonomyFinancialsBaseFrameworkDefinition,
            token,
            storedCompanyId,
            '2023',
            euTaxoFinancialsFixture.t,
            true
          ).then((dataMetaInformation) => {
            cy.ensureLoggedIn(admin_name, admin_pw);
            cy.intercept(`**/api/data/${DataTypeEnum.EutaxonomyFinancials}/${dataMetaInformation.dataId}`).as(
              'fetchDataForPrefill'
            );
            cy.visitAndCheckAppMount(
              '/companies/' +
                storedCompanyId +
                '/frameworks/' +
                DataTypeEnum.EutaxonomyFinancials +
                '/upload?reportingPeriod=' +
                dataMetaInformation.reportingPeriod
            );
            cy.wait('@fetchDataForPrefill', {
              timeout: Cypress.env('medium_timeout_in_ms') as number,
            });
            cy.get('h1').should('contain', euTaxoFinancialsFixture.companyInformation.companyName);

            uploadReports.selectFile(TEST_PDF_FILE_NAME);
            uploadReports.validateReportToUploadHasContainerInTheFileSelector(TEST_PDF_FILE_NAME);
            uploadReports.validateReportToUploadHasContainerWithInfoForm(TEST_PDF_FILE_NAME);

            uploadReports.selectFile(`${TEST_PDF_FILE_NAME}2`);
            uploadReports.validateReportToUploadHasContainerInTheFileSelector(`${TEST_PDF_FILE_NAME}2`);
            uploadReports.validateReportToUploadHasContainerWithInfoForm(`${TEST_PDF_FILE_NAME}2`);

            uploadReports.fillAllFormsOfReportsSelectedForUpload(2);

            selectReportForDatapoint('Total (gross) Carrying Amount', TEST_PDF_FILE_NAME);

            selectReportForDatapoint(
              'Total Amount of Assets towards Taxonomy-relevant Sectors (Taxonomy-eligible)',
              `${TEST_PDF_FILE_NAME}2`
            );

            cy.intercept(
              {
                method: 'POST',
                url: `**/api/data/**`,
                times: 1,
              },
              (request) => {
                const data = assertDefined((request.body as CompanyAssociatedDataEutaxonomyFinancialsData).data);
                expect(TEST_PDF_FILE_NAME in assertDefined(data.general?.general?.referencedReports)).to.equal(
                  areBothDocumentsStillUploaded
                );
                expect(`${TEST_PDF_FILE_NAME}2` in assertDefined(data.general?.general?.referencedReports)).to.equal(
                  true
                );
              }
            ).as('postDataWithTwoReports');
            cy.get('button[data-test="submitButton"]').click();
            cy.wait('@postDataWithTwoReports', { timeout: Cypress.env('short_timeout_in_ms') as number }).then(
              (interception) => {
                expect(interception.response?.statusCode).to.eq(200);
              }
            );
            cy.get('[data-test="datasets-table"]').should('be.visible');
            checkIfLinkedReportsAreDownloadable(storedCompanyId);
            gotoEditForm(storedCompanyId, true);
            uploadReports.selectMultipleFilesAtOnce([TEST_PDF_FILE_NAME, `${TEST_PDF_FILE_NAME}2`]);
            cy.get('.p-dialog.p-component').should('exist').get('[data-pc-section="closebutton"]').click();
            cy.get('.p-dialog.p-component').should('not.exist');

            uploadReports.removeAlreadyUploadedReport(TEST_PDF_FILE_NAME).then(() => {
              areBothDocumentsStillUploaded = false;
            });

            cy.intercept(
              {
                method: 'POST',
                url: `**/api/data/**`,
                times: 1,
              },
              (request) => {
                const data = assertDefined((request.body as CompanyAssociatedDataEutaxonomyFinancialsData).data);
                expect(TEST_PDF_FILE_NAME in assertDefined(data.general?.general?.referencedReports)).to.equal(
                  areBothDocumentsStillUploaded
                );
                expect(`${TEST_PDF_FILE_NAME}2` in assertDefined(data.general?.general?.referencedReports)).to.equal(
                  true
                );
              }
            ).as('postDataWithOneReport');
            cy.get('button[data-test="submitButton"]').click();
            cy.wait('@postDataWithOneReport', { timeout: Cypress.env('short_timeout_in_ms') as number }).then(
              (interception) => {
                expect(interception.response?.statusCode).to.eq(200);
              }
            );
            cy.get('[data-test="datasets-table"]').should('be.visible');
            gotoEditForm(storedCompanyId, false);
          });
        });
      });
    });
  }
);
