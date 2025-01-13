//@ts-nocheck
import DownloadDatasetModal from '@/components/general/DownloadDatasetModal.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { ExportFileTypes } from '@/types/ExportFileTypes.ts';

describe('Component test for DownloadDatasetModal', () => {
  it('DownloadDatasetModal component works correctly', () => {
    cy.mountWithPlugins(DownloadDatasetModal, {
      data() {
        return {
          reportingPeriods: ['2022', '2023'],
          exportFileTypes = [ExportFileTypes.CsvFile, ExportFileTypes.ExcelFile, ExportFileTypes.JsonFile],
          selectedReportingPeriod: '',
          selectedFileFormat: '',
          isModalVisible: true,
          showReportingPeriodError: false,
          showFileFormatError: false,
        };
      },
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      cy.get('[data-test="downloadModal"]').should('exist').should('be.visible');
      cy.get('[data-test="reportingYearSelector"]').should('exist');
      cy.get('[data-test="formatSelector"]').should('exist');
      cy.get('button[data-test=downloadDataButtonInModal]').should('exist').click();

      cy.get('p[data-test=reportingYearError]')
        .should('be.visible')
        .and('contain.text', 'Please select a reporting period.');
      cy.get('p[data-test=fileFormatError]').should('be.visible').and('contain.text', 'Please select a file format.');

      cy.get('[data-test="reportingYearSelector"]').select('2022');
      cy.get('[data-test="formatSelector"]').select('JavaScript Object Notation (.json)');
      cy.get('button[data-test=downloadDataButtonInModal]').click();
      cy.get('[data-test=downloadModal]').should('not.exist');
    });
  });
});
