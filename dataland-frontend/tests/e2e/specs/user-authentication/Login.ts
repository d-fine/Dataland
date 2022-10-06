import { login, logout } from "@e2e/utils/Auth";
import { getBaseUrl } from "../../utils/Cypress";

describe("As a user I want to be able to login and I want the login page to behave as I expect", () => {
  it("Checks that login & logout works", () => {
    login();
    logout();
  });

  it("Checks that the back button on the login page works as expected", () => {
    cy.visit("/companies").get("#back_button").should("exist").click().url().should("eq", `${getBaseUrl()}/`);
  });
});
