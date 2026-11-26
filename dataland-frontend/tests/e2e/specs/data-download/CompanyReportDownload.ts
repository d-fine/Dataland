import { describeIf } from '@e2e/support/TestUtility.ts';
import { type LksgData, type StoredCompany } from '@clients/backend';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures.ts';
import { getBaseUrl } from '@e2e/utils/Cypress.ts';
import { getAdminToken, getUploaderToken } from '@e2e/utils/Auth.ts';
import { generateDummyCompanyInformation, getOrUploadCompanyViaApi } from '@e2e/utils/CompanyUpload.ts';
import { addCompanyIdToDocument, patchDocumentMetaInfo, uploadDocumentViaApi } from '@e2e/utils/DocumentUploadUtils.ts';
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
      cy.get('[data-test="document-download-button"]').should('exist').click();
    }

    before(() => {
      cy.fixture('CompanyInformationWithLksgPreparedFixtures').then((jsonContent) => {
        const preparedFixturesLksg = jsonContent as Array<FixtureData<LksgData>>;
        getPreparedFixture('lksg-all-fields', preparedFixturesLksg);
      });

      getAdminToken()
        .then((token: string) => {
          const uniqueCompanyMarker = Date.now().toString();
          const testStoredCompanyName = 'Company-Created-For-Download-Test-' + uniqueCompanyMarker;
          return getOrUploadCompanyViaApi(token, generateDummyCompanyInformation(testStoredCompanyName));
        })
        .then((newStoredCompany) => {
          storedCompany = newStoredCompany;
        });

      cy.readFile(`../${TEST_PDF_REPORT_FILE_PATH}`, null).then((buffer) => {
        const arrayBuffer = Uint8Array.from(buffer).buffer;
        getUploaderToken().then(async (token: string) => {
          documentMetaInfoResponse = await uploadDocumentViaApi(token, arrayBuffer, `${documentName}.pdf`);
          return documentMetaInfoResponse;
        });
      });
    });

    beforeEach(() => {
      cy.ensureLoggedInAsReader();
    });

    it('Download document, check for file name and appropriate size, and delete it afterwards', () => {
      // The test report is shared with other tests (e.g. the judgement tests run in parallel against the same server).
      // Therefore the company id must only be added to the existing list and never replace it.
      const documentMetaInfoPatch: DocumentMetaInfoPatch = {
        documentName: documentName,
      };
      getAdminToken().then((token: string) => {
        return patchDocumentMetaInfo(token, documentMetaInfoResponse.documentId, documentMetaInfoPatch).then(() =>
          addCompanyIdToDocument(token, documentMetaInfoResponse.documentId, storedCompany.companyId)
        );
      });

      visitPageAndClickDownloadButton();
      cy.get(`a[data-test="report-${TEST_PDF_REPORT_FILE_NAME}-link`);
    });
  }
);
