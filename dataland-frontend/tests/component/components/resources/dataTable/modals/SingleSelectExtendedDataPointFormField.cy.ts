import { mountEditFormField } from '@ct/testUtils/MountEditFormField.ts';
import SingleSelectExtendedDataPointFormField from '@/components/resources/dataTable/modals/SingleSelectExtendedDataPointFormField.vue';

describe('Component test for SingleSelectExtendedDataPointFormField', () => {
  ['extendedEnumPcafMainSector', 'extendedEnumSfdrMainPcafSector'].forEach((dataPointTypeId) => {
    it(`maps the displayed value to the technical value and allows changing it for ${dataPointTypeId}`, () => {
      mountEditFormField(
        SingleSelectExtendedDataPointFormField,
        { value: 'Real Estate', quality: 'Reported', comment: '' },
        { dataPointTypeId }
      );
      cy.get('@component').then((component: { buildApiBodyWithExtendedInfo: () => string }) => {
        expect(JSON.parse(component.buildApiBodyWithExtendedInfo()).value).to.equal('RealEstate');
      });
      cy.get('[data-test="single-select"]').click();
      cy.get('[role="option"]').contains('Health Care').click();
      cy.get('@component').then((component: { buildApiBodyWithExtendedInfo: () => string }) => {
        expect(JSON.parse(component.buildApiBodyWithExtendedInfo()).value).to.equal('HealthCare');
      });
    });
  });
});
