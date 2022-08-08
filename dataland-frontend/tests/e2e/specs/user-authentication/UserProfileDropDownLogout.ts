import { login } from "../../utils/Auth";

describe("User Profile Dropdown logout", () => {
  it("Checks that user dropdown menu logout works", () => {
    login();
    cy.visit("/searchtaxonomy")
      .get("div[id='profile-picture-dropdown-toggle']")
      .click()
      .get("a[id='profile-picture-dropdown-toggle']")
      .click()
      .url()
      .should("eq", Cypress.config("baseUrl") + "/")
      .get("button[name='login_dataland_button']")
      .should("exist")
      .should("be.visible");
  });
});
