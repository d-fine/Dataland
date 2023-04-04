import { login } from "@e2e/utils/Auth";
import { assertDefined } from "@/utils/TypeScriptUtils";

type TokenResponse = { id_token: string; access_token: string; refresh_token: string };

describe("The page should behave well-defined when the user logs out in a different tab or the session expires", () => {
  it("Tests that the popup gets displayed when the user gets logged out in the background", () => {
    cy.intercept("**/token").as("tokenResponse");
    login();
    cy.wait("@tokenResponse").then(({ response }) => {
      const responseTyped = response as { body: TokenResponse };
      const idToken = assertDefined(responseTyped.body?.id_token);

      const logoutUrl = `/keycloak/realms/datalandsecurity/protocol/openid-connect/logout?id_token_hint=${idToken}`;
      cy.request(logoutUrl);
      cy.get("button[name=login_dataland_button]", { timeout: 10000 }).should("exist");
    });
  });

  /**
   * Modifies the given JWT to have the new expiry time. Does NOT update the signature
   *
   * @param jwt the jwt to modify
   * @param newExpiryTime the new expiry time
   * @returns the modified jwt
   */
  function setJwtExpiryTime(jwt: string, newExpiryTime: number): string {
    const split = jwt.split(".");
    const decodedBody = JSON.parse(Buffer.from(split[1], 'base64').toString('binary')) as { exp: number };
    decodedBody.exp = newExpiryTime;
    split[1] = Buffer.from(JSON.stringify(decodedBody), 'binary').toString('base64');
    return split.join(".");
  }

  it("Tests that the popup gets displayed when the refresh token expires", () => {
    let cachedTokenResponse: TokenResponse | null = null;
    cy.intercept("**/token", (req) => {
      if (cachedTokenResponse) {
        req.reply({
          body: cachedTokenResponse,
          statusCode: 200,
        });
      } else {
        req.continue((res) => {
          const body = res.body as TokenResponse;
          const newExpTime = (new Date().getTime() + 30) / 1000;
          body.refresh_token = setJwtExpiryTime(body.refresh_token, newExpTime);
          cachedTokenResponse = body;
        });
      }
    });

    login();
    cy.get("button[name=refresh_session_button]", { timeout: 10000 }).should("exist");
  });
});
