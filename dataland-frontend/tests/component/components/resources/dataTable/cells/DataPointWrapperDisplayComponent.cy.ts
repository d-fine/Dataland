import DataPointWrapperDisplayComponent from '@/components/resources/dataTable/cells/DataPointWrapperDisplayComponent.vue';
import { MLDTDisplayComponentName } from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { getMountingFunction } from '@ct/testUtils/Mount.ts';
import { runFunctionBlockWithinPrimeVueModal } from '@sharedUtils/ElementChecks';

const metaInfo = { dataId: 'dataId', dataType: 'sfdr' };

/**
 * Mounts the component with the given additional display value properties
 * @param displayValue properties added to the display value of the data point wrapper
 */
function mountWithDisplayValue(displayValue: Record<string, unknown>, innerValue: string = 'None'): void {
  getMountingFunction({
    dialogOptions: {
      mountWithDialog: true,
      propsToPassToTheMountedComponent: {
        content: {
          displayComponentName: MLDTDisplayComponentName.DataPointWrapperDisplayComponent,
          displayValue: {
            fieldLabel: 'Assurance',
            innerContents: {
              displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
              displayValue: innerValue,
            },
            ...displayValue,
          },
        },
        metaInfo,
      },
    },
  })(DataPointWrapperDisplayComponent);
}

describe('Component tests for DataPointWrapperDisplayComponent', () => {
  it('offers the modal and opens it if only a provider exists', () => {
    mountWithDisplayValue({ provider: 'Only Provider' });
    cy.get('a.link').click();
    runFunctionBlockWithinPrimeVueModal(() => {
      cy.contains('Only Provider').should('exist');
    });
  });

  it('shows the plain value without modal link if no modal-worthy property exists', () => {
    mountWithDisplayValue({});
    cy.get('a.link').should('not.exist');
    cy.contains('None').should('exist');
  });

  it('renders an empty cell if there is neither a value nor a modal-worthy property', () => {
    mountWithDisplayValue({}, '');
    cy.get('a.link').should('not.exist');
    cy.get('div.flex').should('have.text', '');
  });
});
