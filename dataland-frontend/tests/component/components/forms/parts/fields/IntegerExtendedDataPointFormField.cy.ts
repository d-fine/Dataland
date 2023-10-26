import IntegerExtendedDataPointFormField from "@/components/forms/parts/fields/IntegerExtendedDataPointFormField.vue";

describe("...", () => {
    it("...", () => {
        cy.mountWithPlugins(IntegerExtendedDataPointFormField, {}).then((mounted) => {
            void mounted.wrapper.setProps({
                name: "foo",
                unit: "",
            });
            cy.get('select[name="currency"]').should("exist");
            cy.get('select[name="currency"]').should("contain", "3");
            cy.get('input[name="currency"][type="hidden"]').should("not.exist");
            cy.get('div[data-test="dataQuality"] select[name="quality"]').should("have.value", "NA");
            cy.get('div[data-test="dataQuality"] .form-field-label span.asterisk').should("not.exist");
        });
    });
})