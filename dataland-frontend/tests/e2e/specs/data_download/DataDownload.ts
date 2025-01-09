import ViewFrameworkBase from '@/components/generics/ViewFrameworkBase.vue';
import { mount } from 'cypress/vue';
import * as path from 'path';

describe('As a user, I want to be able to download datasets from Dataland', () => {
  it('should download data as csv file, check for appropriate size and delete it afterwards', () => {
    const fileFormat = 'csv';
    const dataId = '1234'; //get id from existing dataset
    const fileName = `${dataId}.${fileFormat}`;

    //get to the right page as user (https://dataland.com/companies/${existingCompanyId}/frameworks/${existingDataType}
    mount(ViewFrameworkBase, {});

    cy.spy(ViewFrameworkBase.prototype, 'handleDatasetDownload').as('handleDownload');
    cy.spy(ViewFrameworkBase.prototype, 'forceFileDownload').as('forceDownload');

    cy.get('button[data-test=downloadDataButton]').should('exist').click();
    cy.get('[data-test=downloadModal]')
      .should('exist')
      .within(() => {
        cy.get('[data-test="reportingYearSelector"]')
          .find('option')
          .eq(0)
          .invoke('text')
          .then((text) => {
            cy.get('[data-text="reportingYearSelector').select(text);
          });
        cy.get('[data-test="formatSelector"]').select(fileFormat);
        cy.get('button[data-test=downloadDataButtonInModal]').click();
      });

    cy.get('@handleDownload').should('have.been.called');
    cy.get('@forceDownload').should('have.been.called');

    const filePath = path.join(Cypress.config('downloadsFolder'), fileName);
    cy.readFile(filePath, { timeout: 10000 }).should('exist');

    cy.task('getFileSize', filePath).then((size) => {
      const minimumFileSize = 5000; // in bytes ( file should be around 35 KB, only {object}{object} in file is 1 KB)
      expect(size).to.be.greaterThan(minimumFileSize);
    });

    cy.task('deleteFile', filePath).then(() => {
      cy.readFile(filePath).should('not.exist');
    });
  });
});
