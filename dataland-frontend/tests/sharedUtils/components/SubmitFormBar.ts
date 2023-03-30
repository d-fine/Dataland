const barSelector = "[data-test='submitFormBar']";
const submitButtonSelector = "[data-test='submitFormBar'] [data-test='submitButton']";

export const submitFormBar = {
  exists(): void {
    cy.get(barSelector).should("exist");
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
