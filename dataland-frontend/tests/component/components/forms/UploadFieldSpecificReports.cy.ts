// @ts-nocheck
import CreateSfdrDataset from '@/components/forms/CreateSfdrDataset.vue';
import CreateP2pDataset from '@/components/forms/CreateP2pDataset.vue';
import CreateLksgDataset from '@/components/forms/CreateLksgDataset.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { submitButton } from '@sharedUtils/components/SubmitButton';
import { DataTypeEnum } from '@clients/backend';
import { UploadDocuments } from '@sharedUtils/components/UploadDocuments';
import { selectItemFromDropdownByValue } from '@sharedUtils/Dropdown';

const createSfdrDataset = {
  fillRequiredFields(): void {
    this.fillDateFieldWithFutureDate('dataDate');
    cy.get('div[data-test="fiscalYearDeviation"]').get('input[value="Deviation"]').click();
    this.fillDateFieldWithFutureDate('fiscalYearEnd');
  },
  fillDateFieldWithFutureDate(fieldName: string): void {
    cy.get(`[data-test="${fieldName}"] button`).should('have.class', 'p-datepicker-trigger').click();
    cy.get(`input[name="${fieldName}"]`).should('not.be.visible');
    cy.get('div.p-datepicker').find('button[aria-label="Next Month"]').click();
    cy.get('div.p-datepicker').find('span:contains("11")').click();
  },
};

const createP2pDataset = {
  fillRequiredFields(): void {
    createSfdrDataset.fillDateFieldWithFutureDate('dataDate');
    this.selectSector('Automotive');
  },
  selectSector(sector: string): void {
    cy.get('div[data-test="sectors"] div.p-multiselect').should('exist').click();
    cy.contains('span', sector).should('exist').click();
    cy.get('div[data-test="sectors"] div.p-multiselect').should('exist').click();
  },
};
const createLksgDataset = {
  fillRequiredFields(): void {
    createSfdrDataset.fillDateFieldWithFutureDate('dataDate');
  },
};

describe('Component tests for the CreateSfdrDataset that test report uploading', () => {
  const hashForFileWithOneByteSize = '6e340b9cffb37a989ca544e6bb780a2c78901d3fb33738768511a30617afa01d';
  const hashForFileWithTwoBytesSize = '96a296d224f285c67bee93c30f8a309157f0daa35dc5b87e410b78630a09cfc7';

  /**
   * Adds a dummy file to the referenced reports on the SFDR upload page
   * @param fileName name of the file to be referenced
   * @param contentSize number of bytes in dummy file
   */
  function uploadAndReferenceSfdrReferencedReport(fileName: string, contentSize: number): void {
    new UploadDocuments('referencedReports').selectDummyFile(fileName, contentSize);
    cy.get(`div[data-test='scope${contentSize}GhgEmissionsInTonnes'] [data-test='dataPointToggleButton']`).click();
    selectItemFromDropdownByValue(
      cy.get(`div[data-test='scope${contentSize}GhgEmissionsInTonnes'] div[name='fileName']`),
      fileName
    );
  }

  /**
   * Adds a dummy file under a given yes no field
   * @param fileName name to give to the dummy file
   * @param contentSize bytes for dummy data
   * @param fieldName name of the field under which the report should be added
   */
  function uploadFieldSpecificDocuments(fileName: string, contentSize: number, fieldName: string): void {
    cy.get(`[data-test=BaseDataPointFormField${fieldName}]`).find('input[id*="-option-yes"]').check();
    new UploadDocuments(fieldName).selectDummyFile(fileName, contentSize);
  }

  /**
   * Intercepts the upload of a report with given hash
   * @param hash hash of the report to be uploaded
   */
  function interceptEachUpload(hash: string): void {
    console.log(hash, 'XAA');
    cy.intercept('HEAD', '**/documents/' + hash, (request) => {
      request.reply(200, {});
    }).as(`documentExists-${hash}`);
  }

  /**
   * Mounts the upload page and intercept report upload information
   * @param framework the framework to be mounted
   */
  function mountPluginAndInterceptUploads(framework: string): void {
    const companyId = 'company-id';
    let createDataset;
    let dataType: DataTypeEnum;
    if (framework == 'sfdr') {
      dataType = DataTypeEnum.Sfdr;
      createDataset = CreateSfdrDataset;
    } else if (framework == 'p2p') {
      dataType = DataTypeEnum.P2p;
      createDataset = CreateP2pDataset;
    } else {
      dataType = DataTypeEnum.Lksg;
      createDataset = CreateLksgDataset;
    }

    cy.intercept(`**/documents/*`, cy.spy().as('documentExists'));
    cy.intercept('POST', `/api/data/${dataType}*`, {
      statusCode: 200,
    });
    cy.mountWithPlugins(createDataset, {
      keycloak: minimalKeycloakMock({}),

      // @ts-ignore
      props: {
        companyID: companyId,
      },
    });
  }

  it('Check if the document uploads in Sfdr upload page do not interfere', () => {
    const setOfHashesThatShouldBeCheckedForExistence = new Set([
      hashForFileWithOneByteSize,
      hashForFileWithTwoBytesSize,
    ]);
    setOfHashesThatShouldBeCheckedForExistence.forEach((hash) => {
      interceptEachUpload(hash);
    });
    mountPluginAndInterceptUploads('sfdr');
    createSfdrDataset.fillRequiredFields();
    uploadAndReferenceSfdrReferencedReport('Sfdr1', 1);
    uploadAndReferenceSfdrReferencedReport('Sfdr2', 2);
    cy.wait(100);
    submitButton.buttonAppearsEnabled();
    submitButton.clickButton();

    setOfHashesThatShouldBeCheckedForExistence.forEach((hash) => {
      cy.wait(`@documentExists-${hash}`);
    });
    cy.wait(100);
    cy.get('@documentExists').should('have.been.calledTwice');
  });

  it('Check if the document uploads in Lksg upload page still work properly if some document got removed or replaced', () => {
    const setOfHashesThatShouldBeCheckedForExistence = new Set([hashForFileWithTwoBytesSize]);
    setOfHashesThatShouldBeCheckedForExistence.forEach((hash) => {
      interceptEachUpload(hash);
    });
    mountPluginAndInterceptUploads('lksg');
    createLksgDataset.fillRequiredFields();
    uploadFieldSpecificDocuments('first', 1, 'riskManagementSystem');
    cy.wait(100);
    cy.get('div[data-test="BaseDataPointFormFieldriskManagementSystem"] button .pi-times').click();
    uploadFieldSpecificDocuments('second', 2, 'riskManagementSystem');
    uploadFieldSpecificDocuments('fourth', 3, 'grievanceHandlingMechanism');
    cy.wait(100);
    cy.get('div[data-test="BaseDataPointFormFieldgrievanceHandlingMechanism"] button .pi-times').click();
    cy.wait(100);
    submitButton.buttonAppearsEnabled();
    submitButton.clickButton();

    setOfHashesThatShouldBeCheckedForExistence.forEach((hash) => {
      cy.wait(`@documentExists-${hash}`);
    });
    cy.wait(100);
    cy.get('@documentExists').should('have.been.calledOnce');
  });

  it('Check if the document uploads in P2p upload page work', () => {
    const setOfHashesThatShouldBeCheckedForExistence = new Set([hashForFileWithOneByteSize]);
    setOfHashesThatShouldBeCheckedForExistence.forEach((hash) => {
      interceptEachUpload(hash);
    });
    mountPluginAndInterceptUploads('p2p');
    createP2pDataset.fillRequiredFields();
    uploadFieldSpecificDocuments('first', 1, 'upstreamSupplierProcurementPolicy');
    createP2pDataset.selectSector('Livestock Farming');
    uploadFieldSpecificDocuments('second', 2, 'externalFeedCertification');
    createP2pDataset.selectSector('Livestock Farming');
    cy.wait(100);
    submitButton.buttonAppearsEnabled();
    submitButton.clickButton();

    setOfHashesThatShouldBeCheckedForExistence.forEach((hash) => {
      cy.wait(`@documentExists-${hash}`);
    });
    cy.wait(100);
    cy.get('@documentExists').should('have.been.calledOnce');
  });
});
