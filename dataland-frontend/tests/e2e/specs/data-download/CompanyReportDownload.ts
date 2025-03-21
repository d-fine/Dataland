import { describeIf } from '@e2e/support/TestUtility.ts';
import { type LksgData, type StoredCompany } from '@clients/backend';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures.ts';
import {
  admin_name,
  admin_pw,
  getBaseUrl,
  reader_name,
  reader_pw,
  uploader_name,
  uploader_pw,
} from '@e2e/utils/Cypress.ts';
import { getKeycloakToken } from '@e2e/utils/Auth.ts';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload.ts';
import { patchDocumentMetaInfo, uploadDocumentViaApi } from '@e2e/utils/DocumentUpload.ts';
import { type DocumentMetaInfoPatch, type DocumentMetaInfoResponse } from '@clients/documentmanager';
import { TEST_PDF_REPORT_FILE_NAME, TEST_PDF_REPORT_FILE_PATH } from '@sharedUtils/ConstantsForPdfs.ts';

describeIf(
  'As a user, I want to be able to download company reports and other documents from Dataland',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    const documentName = 'test-report';
    let documentMetaInfoResponse: DocumentMetaInfoResponse;

    let storedCompany: StoredCompany;

    /**
     * Visit documents page and clicks download button for first entry
     */
    function visitPageAndClickDownloadButton(): void {
      cy.visit(getBaseUrl() + `/companies/${storedCompany.companyId}/documents`);
      cy.get('[data-test="download-button"]').should('exist').click();
    }

    before(() => {
      cy.fixture('CompanyInformationWithLksgPreparedFixtures').then((jsonContent) => {
        const preparedFixturesLksg = jsonContent as Array<FixtureData<LksgData>>;
        getPreparedFixture('lksg-all-fields', preparedFixturesLksg);
      });

      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        const uniqueCompanyMarker = Date.now().toString();
        const testStoredCompanyName = 'Company-Created-For-Download-Test-' + uniqueCompanyMarker;
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(testStoredCompanyName)).then(
          (newStoredCompany) => {
            storedCompany = newStoredCompany;
          }
        );
      });

      cy.readFile(`../${TEST_PDF_REPORT_FILE_PATH}`, 'base64').then((base64String) => {
        const fileBuffer = Buffer.from(base64String, 'base64');
        getKeycloakToken(uploader_name, uploader_pw).then(async (token: string) => {
          documentMetaInfoResponse = await uploadDocumentViaApi(token, fileBuffer, documentName);
          return documentMetaInfoResponse;
        });
      });
    });

    beforeEach(() => {
      cy.ensureLoggedIn(reader_name, reader_pw);
    });

    it('Download document, check for file name and appropriate size, and delete it afterwards', () => {
      const documentMetaInfoPatch: DocumentMetaInfoPatch = {
        documentName: documentName,
        companyIds: [storedCompany.companyId] as unknown as Set<string>,
      };
      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return patchDocumentMetaInfo(token, documentMetaInfoResponse.documentId, documentMetaInfoPatch);
      });

      visitPageAndClickDownloadButton();
      cy.get(`a[data-test="report-${TEST_PDF_REPORT_FILE_NAME}-link`);
    });
  }
);
