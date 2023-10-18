import TestLanguageDependentContent from "./TestLanguageDependentContent.vue";

describe("Component test for LanguageDependentContent", () => {
  it("Should display different content depending on the selected language", () => {
    cy.mountWithPlugins(TestLanguageDependentContent, {}).then(() => {
      cy.get(".p-dropdown").should("exist");
      cy.get(".p-dropdown-label").should("contain.text", "English");
      cy.contains("English content").should("exist");
      cy.contains("Deutsche Inhalte").should("not.exist");
      cy.get(".p-dropdown").click().get(".p-dropdown-items li").eq(1).click();
      cy.get(".p-dropdown-label").should("contain.text", "German");
      cy.contains("English content").should("not.exist");
      cy.contains("Deutsche Inhalte").should("exist");
    });
  });
});
