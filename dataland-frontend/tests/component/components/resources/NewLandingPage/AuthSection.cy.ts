import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import AuthSection from "@/components/resources/newLandingPage/AuthSection.vue";
import {checkAnchorByContent, checkButton} from "../../../testUtils/ExistenceChecks";

describe("Component test for the AuthSection component", () => {
  it("Check if an error in login or register occurs the process is canceled", () => {
    const keycloakMock = minimalKeycloakMock({
        authenticated: false,
      });
    keycloakMock.createLoginUrl = () => { throw Error("Login Error!") };
    keycloakMock.createRegisterUrl = () => { throw Error("Register Error!") };
    cy.mountWithPlugins(AuthSection, { keycloak: keycloakMock }).then(() => {
      checkButton("signup_dataland_button", "Sign Up").click();
      checkAnchorByContent("Login").click();
    });
  });
});
