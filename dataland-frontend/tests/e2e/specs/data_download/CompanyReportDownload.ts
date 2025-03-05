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
import { addCompanyToDocumentMetaInfoViaApi, uploadDocumentViaApi } from '@e2e/utils/DocumentUpload.ts';
import { type DocumentMetaInfoResponse } from '@clients/documentmanager';
import { join } from 'path';

describeIf(
  'As a user, I want to be able to download company reports and other documents from Dataland',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    const documentName = 'test-report.pdf';
    let documentMetaInfoResponse: DocumentMetaInfoResponse;

    let storedCompany: StoredCompany;

    /**
     * Checks that the downloaded file does actually exist and delete it
     * @param filePath path to file
     */
    function checkThatFileExistsAndDelete(filePath: string): void {
      cy.readFile(filePath, { timeout: Cypress.env('short_timeout_in_ms') as number }).should('exist');
      cy.task('deleteFile', filePath).then(() => {
        cy.readFile(filePath).should('not.exist');
      });
    }

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

      cy.readFile('../testing/data/documents/test-report.pdf', 'base64').then((base64String) => {
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
      getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return addCompanyToDocumentMetaInfoViaApi(token, documentMetaInfoResponse.documentId, storedCompany.companyId);
      });

      visitPageAndClickDownloadButton();

      const filePath = join(Cypress.config('downloadsFolder'), documentName);
      checkThatFileExistsAndDelete(filePath);
    });
  }
);
