import ApiKeysPage from "@/components/pages/ApiKeysPage.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";

describe("Component test for ApiKeyCard.vue", () => {
  it("If api key already exists, it should display proper user role", () => {
    cy.intercept("GET", "**/api-keys/getApiKeyMetaInfoForUser", { fixture: "ApiKeyInfoMockWithKey.json" });
    cy.mountWithPlugins(ApiKeysPage, {
      keycloak: minimalKeycloakMock({}),
      data() {
        return {
          newKey: "abcdefghijklmnoprstwxyz123456789",
          userAlreadyHasApiKey: true,
          pageState: "view",
          waitingForData: false,
        };
      },
    });
    cy.get('[data-test="apiKeyInfo"]').find("textarea").should("have.attr", "readonly");
    cy.get('[data-test="apiKeyInfo"]').find("textarea").invoke("val").should("eq", "abcdefghijklmnoprstwxyz123456789");
  });
});
