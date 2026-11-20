import { mountEditFormField } from '@ct/testUtils/MountEditFormField.ts';
import RadioButtonsExtendedDataPointFormField from '@/components/resources/dataTable/modals/RadioButtonsExtendedDataPointFormField.vue';

type ComponentWithApiBody = { buildApiBodyWithExtendedInfo: () => string };

const fiscalYearTypeId = 'extendedEnumFiscalYearDeviation';
const radioSelector = '[data-test="radio-buttons-select"]';

/**
 * @param extendedDataPointObject the initial data point
 * @param dataPointTypeId the optional data point type id
 */
function mountComponent(extendedDataPointObject: object, dataPointTypeId?: string): void {
  mountEditFormField(RadioButtonsExtendedDataPointFormField, extendedDataPointObject, { dataPointTypeId });
}

/**
 * Asserts that the value in the API body built by the component equals the expected value
 * @param expected the expected technical value
 */
function expectBuiltValue(expected: string): void {
  cy.get('@component').then((component: ComponentWithApiBody) => {
    expect(JSON.parse(component.buildApiBodyWithExtendedInfo()).value).to.equal(expected);
  });
}

/**
 * Asserts that the API body built by the component contains no value
 */
function expectNoBuiltValue(): void {
  cy.get('@component').then((component: ComponentWithApiBody) => {
    expect(JSON.parse(component.buildApiBodyWithExtendedInfo()).value).to.be.undefined;
  });
}

/**
 * Clicks an option and asserts the resulting technical value
 * @param optionLabel the displayed label of the option
 * @param expected the expected technical value
 */
function selectOptionAndExpect(optionLabel: string, expected: string): void {
  cy.get(radioSelector).contains(optionLabel).click();
  expectBuiltValue(expected);
}

describe('Component test for RadioButtonsExtendedDataPointFormField', () => {
  it('maps the humanized displayed value to the technical enum value', () => {
    mountComponent({ value: 'No Deviation', quality: 'Reported', comment: '' }, fiscalYearTypeId);
    expectBuiltValue('NoDeviation');
  });

  it('allows switching the selection', () => {
    mountComponent({ value: 'Deviation' }, fiscalYearTypeId);
    selectOptionAndExpect('No Deviation', 'NoDeviation');
  });

  ['extendedEnumCompanyExchangeStatus', 'extendedEnumSfdrCompanyExchangeStatus'].forEach((typeId) => {
    it(`offers Listed and Unlisted for ${typeId}`, () => {
      mountComponent({ value: 'Listed' }, typeId);
      cy.get(radioSelector).should('contain', 'Listed').and('contain', 'Unlisted');
      cy.get(radioSelector).should('not.contain', 'Deviation');
      selectOptionAndExpect('Unlisted', 'Unlisted');
    });
  });

  it('falls back to the deviation options if no dataPointTypeId is provided', () => {
    mountComponent({ value: 'Deviation' });
    cy.get(radioSelector).should('contain', 'No Deviation');
    cy.get(radioSelector).should('not.contain', 'Listed');
    selectOptionAndExpect('No Deviation', 'NoDeviation');
  });

  it('has no selection for an empty initial value', () => {
    mountComponent({}, fiscalYearTypeId);
    expectNoBuiltValue();
    selectOptionAndExpect('Deviation', 'Deviation');
  });
});
