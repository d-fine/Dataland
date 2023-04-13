const submitButtonSelector = "[data-test='submitButton'] [data-test='submitButton']";

export const submitButton = {
  exists(): void {
    cy.get(submitButtonSelector).should("exist");
  },
  buttonAppearsDisabled(): void {
    cy.get(submitButtonSelector).should("have.class", "button-disabled");
  },
  buttonAppearsEnabled(): void {
    cy.get(submitButtonSelector).should("not.have.class", "button-disabled");
  },
  buttonIsAddDataButton(): void {
    cy.get(submitButtonSelector).contains("ADD DATA").should("exist");
  },
  buttonIsUpdateDataButton(): void {
    cy.get(submitButtonSelector).contains("UPDATE DATA").should("exist");
  },
  clickButton(): void {
    cy.get(submitButtonSelector).click();
  },
};
