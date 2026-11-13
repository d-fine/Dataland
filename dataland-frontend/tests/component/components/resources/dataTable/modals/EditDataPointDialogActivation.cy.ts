import { mountEditFormField } from '@ct/testUtils/MountEditFormField.ts';
import EditDataPointDialog from '@/components/resources/dataTable/modals/EditDataPointDialog.vue';
import { ref } from 'vue';

const metaInfo = (currentlyActive: boolean): object => ({
  dataPointId: 'new-id',
  dataPointType: 'extendedEnumYesNo',
  companyId: 'c',
  reportingPeriod: '2024',
  uploaderUserId: 'u',
  uploadTime: 1,
  currentlyActive,
  qaStatus: 'Accepted',
});

describe('EditDataPointDialog save', () => {
  it('only closes once the posted data point is active', () => {
    const close = cy.stub().as('close');
    let metaInfoRequests = 0;
    cy.intercept('POST', '**/data-points?*', { statusCode: 200, body: metaInfo(false) }).as('post');
    cy.intercept('GET', '**/data-points/new-id/metadata', (req) => {
      metaInfoRequests++;
      req.reply(metaInfo(metaInfoRequests >= 3));
    }).as('metaInfo');

    mountEditFormField(
      EditDataPointDialog,
      {},
      {
        dialogRef: ref({
          data: {
            companyId: 'c',
            reportingPeriod: '2024',
            uploadComponentName: 'YesNoExtendedDataPointFormField',
            dataPointTypeId: 'extendedEnumYesNo',
            dataPoint: { displayValue: { innerContents: { displayValue: 'Yes' }, quality: 'Reported' } },
          },
          close,
        }),
      }
    );
    cy.get('[data-test="save-data-point-button"]').click();
    cy.wait('@post');
    cy.get('@close').should('not.have.been.called');
    cy.get('@close').should('have.been.calledWith', { dataUpdated: true });
    cy.then(() => expect(metaInfoRequests).to.be.at.least(3));
  });
});
