import CurrencyDataPointFormField from "@/components/forms/parts/fields/CurrencyDataPointFormField.vue";

describe("Component test for BigDecimalExtendedDataPointFormField", () => {
  it("Unit field should be visible when options are defined and Quality field should be NA if the value field has no value", () => {
    cy.mountWithPlugins(CurrencyDataPointFormField, {}).then(() => {
      cy.get('[data-test="dataPointToggleButton"]').click();
      cy.get('select[name="currency"]').should("exist");
      cy.get('select[name="currency"]').should("contain", "EUR");
      cy.get('input[name="currency"][type="hidden"]').should("not.exist");
      cy.get('div[data-test="dataQuality"] select[name="quality"]').should("have.value", "NA");
      cy.get('div[data-test="dataQuality"] .form-field-label span.asterisk').should("not.exist");
    });
  });
});
