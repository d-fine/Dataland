import { mountEditFormField } from '@ct/testUtils/MountEditFormField.ts';
import EditDataPointDialog from '@/components/resources/dataTable/modals/EditDataPointDialog.vue';
import { MLDTDisplayObjectForEmptyString } from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { ref } from 'vue';

const dataPointTypeId = 'extendedEuTaxonomyAlignedActivitiesComponentRevenueAlignedActivities';

const emptyShapes: Record<string, unknown> = {
  'data point is null': MLDTDisplayObjectForEmptyString,
  'value is null with NoDataFound': {
    displayValue: {
      modalOptions: {
        data: {
          listOfRowContents: [],
          kpiType: 'revenue',
          dataPointDisplay: { quality: 'NoDataFound', comment: null, dataSource: null },
        },
      },
    },
  },
  'value is an empty list': {
    displayValue: {
      modalOptions: {
        data: {
          listOfRowContents: [],
          kpiType: 'revenue',
          dataPointDisplay: { quality: 'Reported', comment: null, dataSource: { page: '1', fileReference: 'x' } },
        },
      },
    },
  },
};

Object.entries(emptyShapes).forEach(([name, dataPoint]) => {
  it(`offers adding the first activity when the ${name}`, () => {
    mountEditFormField(
      EditDataPointDialog,
      {},
      {
        dialogRef: ref({
          data: {
            companyId: 'c',
            reportingPeriod: '2024',
            uploadComponentName: 'AlignedActivitiesFormField',
            dataPointTypeId,
            dataPoint,
          },
          close: () => {},
        }),
      }
    );
    cy.get('[data-test="add-first-activity-button"]').click();
    cy.get('.add-activity-overlay .p-select-option').first().click();
    cy.get('[data-test="activity-name"]').should('have.length', 1);
  });
});
