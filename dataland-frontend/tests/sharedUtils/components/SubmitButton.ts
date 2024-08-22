const submitButtonSelector = "[data-test='submitButton'] [data-test='submitButton']";

export const submitButton = {
  exists(): void {
    cy.get(submitButtonSelector).should('exist');
  },
  buttonAppearsDisabled(): void {
    cy.get(submitButtonSelector).should(($input) => {
      expect($input).to.have.class('col-12');
      expect($input).to.have.class('m-0');
      expect($input).to.have.class('button-disabled');
    });
  },
  buttonAppearsEnabled(): void {
    cy.get(submitButtonSelector).should(($input) => {
      expect($input).to.have.class('col-12');
      expect($input).to.have.class('m-0');
      expect($input).to.not.have.class('button-disabled');
    });
  },
  buttonIsAddDataButton(): void {
    cy.get(submitButtonSelector).contains('ADD DATA').should('exist');
  },
  buttonIsUpdateDataButton(): void {
    cy.get(submitButtonSelector).contains('UPDATE DATA').should('exist');
  },
  clickButton(): void {
    cy.get(submitButtonSelector).click();
  },
};
