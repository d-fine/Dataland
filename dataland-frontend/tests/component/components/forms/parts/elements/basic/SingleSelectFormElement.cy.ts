import SingleSelectFormElement from "@/components/forms/parts/elements/basic/SingleSelectFormElement.vue";

describe("Component tests for the SingleSelectFormElement", () => {
  const placeholderText = "Empty";
  const nonExistantOption = "This option does not exist";

  it("Selecting an unknown option via API should be allowed and displayed in the UI when enabled", () => {
    cy.mountWithPlugins(SingleSelectFormElement, {}).then((mounted) => {
      void mounted.wrapper.setProps({
        allowUnknownOption: true,
        modelValue: nonExistantOption,
      });

      cy.get("span.p-dropdown-label").should("have.text", nonExistantOption);
    });
  });

  it("An unknown option should be auto-deselected if unknown options are not allowed", () => {
    cy.mountWithPlugins(SingleSelectFormElement, {}).then((mounted) => {
      void mounted.wrapper.setProps({
        placeholder: placeholderText,
        allowUnknownOption: false,
        modelValue: nonExistantOption,
      });

      cy.get("span.p-dropdown-label").should("have.text", placeholderText);
    });
  });

  it("An unknown option should be auto-deselected if the auto-deselection feature is enabled mid-run", () => {
    cy.mountWithPlugins(SingleSelectFormElement, {}).then((mounted) => {
      cy.wrap(mounted.wrapper).then((wrapper) => {
        void wrapper.setProps({
          placeholder: placeholderText,
          allowUnknownOption: true,
          modelValue: nonExistantOption,
        });
        cy.get("span.p-dropdown-label").should("have.text", nonExistantOption);
      });

      cy.wrap(mounted.wrapper).then((wrapper) => {
        void wrapper.setProps({
          allowUnknownOption: false,
        });
        cy.get("span.p-dropdown-label").should("have.text", placeholderText);
      });
    });
  });

  for (const allowUnknownOption of [true, false]) {
    it(
      "Removing the currently selected option from the list of allowed option should result in a deselection " +
        `if allowUnknownOption is set to '${allowUnknownOption}'`,
      () => {
        cy.mountWithPlugins(SingleSelectFormElement, {}).then((mounted) => {
          cy.wrap(mounted.wrapper).then((wrapper) => {
            void wrapper.setProps({
              placeholder: placeholderText,
              allowUnknownOption: allowUnknownOption,
              deselectRemovedOptionsOnShrinkage: true,
              options: ["A", "B"],
              modelValue: "A",
            });

            cy.get("span.p-dropdown-label").should("have.text", "A");
          });

          cy.wrap(mounted.wrapper).then((wrapper) => {
            void wrapper.setProps({
              options: ["B"],
            });

            cy.get("span.p-dropdown-label").should("have.text", placeholderText);
          });
        });
      },
    );
  }
});
