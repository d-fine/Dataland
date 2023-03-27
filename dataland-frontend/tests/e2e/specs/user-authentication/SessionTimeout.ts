import { login } from "@e2e/utils/Auth";
import { assertDefined } from "@/utils/TypeScriptUtils";

describe("The page should behave well-defined when the user logs out in a different tab or the session expires", () => {
  it("Tests that the popup gets displayed when the user gets logged out in the background", () => {
    cy.intercept("**/token").as("tokenResponse");
    login();
    cy.wait("@tokenResponse").then(({ response }) => {
      type TokenResponse = { body: { id_token: string | undefined } | undefined };
      const responseTyped = response as TokenResponse;
      const idToken = assertDefined(responseTyped.body?.id_token);

      const logoutUrl = `/keycloak/realms/datalandsecurity/protocol/openid-connect/logout?id_token_hint=${idToken}`;
      cy.request(logoutUrl);
      cy.get("button[name=login_dataland_button]", { timeout: 10000 }).should("exist");
    });
  });
});
