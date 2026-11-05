import { mountEditFormField } from '@ct/testUtils/MountEditFormField.ts';
import DateExtendedDataPointFormField from '@/components/resources/dataTable/modals/DateExtendedDataPointFormField.vue';

/**
 * @param extendedDataPointObject the initial data point
 */
function mountComponent(extendedDataPointObject: object): void {
  mountEditFormField(DateExtendedDataPointFormField, extendedDataPointObject);
}

describe('Component test for DateExtendedDataPointFormField', () => {
  it('prefills an existing ISO date and returns it in the api body', () => {
    mountComponent({ value: '2024-12-31', quality: 'Reported', comment: 'c' });
    cy.get('[data-test="date-select"] input').should('have.value', '2024-12-31');
    cy.get('@component').then((component: { buildApiBodyWithExtendedInfo: () => string }) => {
      const body = JSON.parse(component.buildApiBodyWithExtendedInfo());
      expect(body.value).to.equal('2024-12-31');
    });
  });

  it('starts empty for a non-date value', () => {
    mountComponent({ value: 'not a date' });
    cy.get('[data-test="date-select"] input').should('have.value', '');
  });
});
