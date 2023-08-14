import DataPointFormField from "@/components/forms/parts/kpiSelection/DataPointFormField.vue";

describe("Component test for DataPointFormField", () => {
  it("Unit field should be visible when options are defined and Quality field should be NA if the value field has no value", () => {
    cy.mountWithPlugins(DataPointFormField, {}).then((mounted) => {
      void mounted.wrapper.setProps({
        evidenceDesired: true,
        unit: "",
        options: [
          { label: "1", value: "2" },
          { label: "3", value: "4" },
        ],
      });
      cy.get('select[name="unit"]').should("exist");
      cy.get('select[name="unit"]').should("contain", "3");
      cy.get('input[name="unit"][type="hidden"]').should("not.exist");
      cy.get('div[data-test="dataQuality"] select[name="quality"]').should("have.value", "NA");
      cy.get('div[data-test="dataQuality"] .form-field-label span.asterisk').should("not.exist");
    });
  });
  it("Unit field should not visible when unit is hardcoded and Quality field should have no value if the value field has value", () => {
    cy.mountWithPlugins(DataPointFormField, {
      data() {
        return {
          currentValue: "1234",
        };
      },
    }).then((mounted) => {
      void mounted.wrapper.setProps({
        evidenceDesired: true,
        unit: "Days",
      });
      cy.get('select[name="unit"]').should("not.exist");
      cy.get('input[name="unit"][type="hidden"]').should("exist");
      cy.get('input[name="unit"][type="hidden"]').should("have.value", "Days");
      cy.get('div[data-test="dataQuality"] select[name="quality"]').should("have.value", null);
      cy.get('div[data-test="dataQuality"] .form-field-label span.asterisk').should("exist");
    });
  });
});
