// @ts-nocheck
import CurrencyDataPointFormField from "@/components/forms/parts/fields/CurrencyDataPointFormField.vue";
import { selectItemFromDropdownByValue } from "@sharedUtils/Dropdown";

describe("Component test for CurrencyDataPointFormField", () => {
  /**
   * Ensures that the data quality field is inactive and that the field is not shown as required with an asterix
   */
  function checkDataQualityFieldNotActive(): void {
    cy.get('div[data-test="dataQuality"] div[name="quality"]')
      .should("have.class", "p-disabled")
      .find("span")
      .should("have.text", "NA");
    cy.get('div[data-test="dataQuality"] .form-field-label span.asterisk').should("not.exist");
  }

  /**
   * Ensures that the data quality field is active and that the field is shown as required with an asterix
   */
  function checkDataQualityFieldActive(): void {
    cy.get('div[data-test="dataQuality"] div[name="quality"]')
      .should("not.have.class", "p-disabled")
      .find("span")
      .should("not.have.text", "NA");
    cy.get('div[data-test="dataQuality"] .form-field-label span.asterisk').should("exist");
  }

  it("Quality field should be NA if the value field has no value and currency field should work as expected", () => {
    cy.mountWithPlugins(CurrencyDataPointFormField, {}).then(() => {
      cy.get('[data-test="dataPointToggleButton"]').click();

      checkDataQualityFieldNotActive();
      cy.get('input[name="value"]').should("be.visible").type("123");
      checkDataQualityFieldActive();

      selectItemFromDropdownByValue(cy.get('div[name="currency"]').should("be.visible"), "EUR", true);
    });
  });
});
