//@ts no-check
import DownloadDatasetModal from '@/components/general/DownloadDatasetModal.vue';
import { DataTypeEnum } from '@clients/backend';

describe('Component test for DownloadDatasetModal', () => {
  it(
    'Should display the error message in download modal when clicking download button without selections ' +
      'and close download modal with selection',
    () => {
      cy.mountWithPlugins(DownloadDatasetModal, {
        props: {
          isDownloadModalOpen: true,
          handleDownload: cy.stub().as('handleDownloadStub'),
          dataType: DataTypeEnum.Sfdr,
          mapOfReportingPeriodToActiveDataset: new Map([
            ['2022', { dataType: DataTypeEnum.Sfdr, reportingPeriod: '2022', currentlyActive: true }],
            ['2023', { dataType: DataTypeEnum.Sfdr, reportingPeriod: '2023', currentlyActive: true }],
          ]),
        },
      });

      cy.get('select[data-test="reportingYearSelector"]').should('exist');
      cy.get('select[data-test="formatSelector"]').should('exist');
      cy.get('button[data-test=downloadDataButtonInModal]').should('exist').click();

      cy.get('p[data-test=noReportingYearError]')
        .should('be.visible')
        .and('contain.text', 'Please select a reporting period.');
      cy.get('p[data-test=noFileFormatError]').should('be.visible').and('contain.text', 'Please select a file format.');

      cy.get('select[data-test="reportingYearSelector"]').select('2022');
      cy.get('select[data-test="formatSelector"]').select('json');
      cy.get('button[data-test=downloadDataButtonInModal]').click();

      cy.get('@handleDownloadStub').should('have.been.calledWith', '2022', 'json');
      cy.get('PrimeDialog[data-test=downloadModal]').should('not.exist');
    }
  );
});
