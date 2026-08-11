import CreateSfdrDataset from '@/components/forms/CreateSfdrDataset.vue';
import CreateLksgDataset from '@/components/forms/CreateLksgDataset.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { submitButton } from '@sharedUtils/components/SubmitButton';
import { DataTypeEnum } from '@clients/backend';
import { UploadDocuments } from '@sharedUtils/components/UploadDocuments';
import { selectItemFromDropdownByValue } from '@sharedUtils/Dropdown';

const createSfdrDataset = {
  fillRequiredFields(): void {
    this.fillDateFieldWithFutureDate('dataDate');

    cy.get('div[data-test="fiscalYearDeviation"]').find('input[value="Deviation"][value="Deviation"]').click();

    cy.get('div[data-test="fiscalYearEnd"] [data-test="dataPointToggleButton"]').click();
    this.fillDateFieldWithFutureDate('fiscalYearEnd');
  },

  fillDateFieldWithFutureDate(fieldName: string): void {
    cy.get(`[data-test="${fieldName}"] button`).should('have.class', 'p-datepicker-dropdown').click();

    cy.get(`[data-test="${fieldName}"] input.formkit-input`).should('not.be.visible');

    cy.get('.p-datepicker-header').find('button[aria-label="Next Month"]').click();

    cy.get('.p-datepicker-day-view').find('span:contains("11")').click();
  },
};

const createLksgDataset = {
  fillRequiredFields(): void {
    createSfdrDataset.fillDateFieldWithFutureDate('dataDate');
  },
};

/**
 * Adds a dummy file to the referenced reports on the SFDR upload page.
 *
 * @param fileName name of the file to be referenced
 * @param contentSize number of bytes in the dummy file
 */
function uploadAndReferenceSfdrReferencedReport(fileName: string, contentSize: number): void {
  new UploadDocuments('referencedReports').selectDummyFile(fileName, contentSize);

  cy.get(`div[data-test='scope${contentSize}GhgEmissionsInTonnes'] [data-test='dataPointToggleButton']`).within(() => {
    cy.get('#dataPointIsAvailableSwitch').click();
  });

  selectItemFromDropdownByValue(
    cy.get(`div[data-test='scope${contentSize}GhgEmissionsInTonnes'] [data-test='dataReport']`),
    fileName
  );
}

/**
 * Adds a dummy file under a given yes/no field.
 *
 * @param fileName name to give to the dummy file
 * @param contentSize bytes for the dummy file
 * @param fieldName name of the field under which the report should be added
 */
function uploadFieldSpecificDocuments(fileName: string, contentSize: number, fieldName: string): void {
  cy.get(`[data-test=BaseDataPointFormField${fieldName}]`).find('input[type="checkbox"][value="Yes"]').check();

  new UploadDocuments(fieldName).selectDummyFile(fileName, contentSize);
}

/**
 * Intercepts the upload of a report with the given hash.
 *
 * @param hash hash of the report to be uploaded
 */
function interceptEachUpload(hash: string): void {
  cy.intercept('HEAD', `**/documents/${hash}`, (request) => {
    request.reply(200, {});
  }).as(`documentExists-${hash}`);
}

/**
 * Mounts the upload page and intercepts report upload information.
 *
 * @param framework framework to be mounted
 */
function mountPluginAndInterceptUploads(framework: string): void {
  const companyId = 'company-id';

  let createDataset;
  let dataType: DataTypeEnum;

  if (framework === 'sfdr') {
    dataType = DataTypeEnum.Sfdr;
    createDataset = CreateSfdrDataset;
  } else {
    dataType = DataTypeEnum.Lksg;
    createDataset = CreateLksgDataset;
  }

  cy.intercept('**/documents/*', cy.spy().as('documentExists'));

  cy.intercept('POST', `/api/data/${dataType}*`, {
    statusCode: 200,
  });

  // @ts-ignore
  cy.mountWithPlugins(createDataset, {
    keycloak: minimalKeycloakMock({}),
    props: {
      companyID: companyId,
    },
  });
}

describe('Component tests for the CreateSfdrDataset that test report uploading', () => {
  const hashForFileWithOneByteSize = '6e340b9cffb37a989ca544e6bb780a2c78901d3fb33738768511a30617afa01d';

  const hashForFileWithTwoBytesSize = '96a296d224f285c67bee93c30f8a309157f0daa35dc5b87e410b78630a09cfc7';

  it('Check if the document uploads in Sfdr upload page do not interfere', () => {
    const setOfHashesThatShouldBeCheckedForExistence = new Set([
      hashForFileWithOneByteSize,
      hashForFileWithTwoBytesSize,
    ]);

    for (const hash of setOfHashesThatShouldBeCheckedForExistence) {
      interceptEachUpload(hash);
    }

    mountPluginAndInterceptUploads('sfdr');

    createSfdrDataset.fillRequiredFields();

    uploadAndReferenceSfdrReferencedReport('Sfdr1', 1);
    cy.contains('Sfdr1').should('be.visible');

    uploadAndReferenceSfdrReferencedReport('Sfdr2', 2);
    cy.contains('Sfdr2').should('be.visible');

    submitButton.buttonAppearsEnabled();
    submitButton.clickButton();

    for (const hash of setOfHashesThatShouldBeCheckedForExistence) {
      cy.wait(`@documentExists-${hash}`);
    }

    cy.get('@documentExists').should('have.been.calledTwice');
  });

  it('Check if the document uploads in Lksg upload page still work properly if some document got removed or replaced', () => {
    const setOfHashesThatShouldBeCheckedForExistence = new Set([hashForFileWithTwoBytesSize]);

    for (const hash of setOfHashesThatShouldBeCheckedForExistence) {
      interceptEachUpload(hash);
    }

    mountPluginAndInterceptUploads('lksg');

    createLksgDataset.fillRequiredFields();

    uploadFieldSpecificDocuments('first', 1, 'riskManagementSystem');
    cy.contains('first').should('be.visible');

    cy.get('div[data-test="BaseDataPointFormFieldriskManagementSystem"] button .pi-times').should('be.visible').click();

    cy.contains('first').should('not.exist');

    uploadFieldSpecificDocuments('second', 2, 'riskManagementSystem');
    cy.contains('second').should('be.visible');

    uploadFieldSpecificDocuments('fourth', 3, 'grievanceHandlingMechanism');
    cy.contains('fourth').should('be.visible');

    cy.get('div[data-test="BaseDataPointFormFieldgrievanceHandlingMechanism"] button .pi-times')
      .should('be.visible')
      .click();

    cy.contains('fourth').should('not.exist');

    submitButton.buttonAppearsEnabled();
    submitButton.clickButton();

    for (const hash of setOfHashesThatShouldBeCheckedForExistence) {
      cy.wait(`@documentExists-${hash}`);
    }

    cy.get('@documentExists').should('have.been.calledOnce');
  });
});
