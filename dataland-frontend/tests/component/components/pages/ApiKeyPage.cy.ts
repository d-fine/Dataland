import ApiKeysPage from "../../../../src/components/pages/ApiKeysPage.vue";
import { mount } from "cypress/vue";

describe("Component test for ApiKeyCard.vue", () => {
  it("Should display proper user role", () => {
    mount(ApiKeysPage, {
      global: {
        provide: {
          authenticated: true,
          getKeycloakPromise() {
            return Promise.resolve({
              authenticated: true,
            });
          },
        },
      },
      data() {
        return {
          newKey: "abcdefghijklmnoprstwxyz123456789",
          existsApiKey: true,
          pageState: "view",
          waitingForData: false,
        };
      },
      props: {
        userRoles: ["ROLE_USER", "ROLE_ADMIN"],
      },
    });
    cy.intercept("GET", "**/api-keys/getApiKeyMetaInfoForUser*", { fixture: "ApiKeyInfoMockWithNOKey.json" }).as(
      "apiKeyInfo"
    );
    cy.get('[data-test="apiKeyInfo"]').find("textarea").should("have.attr", "readonly");
    cy.get('[data-test="apiKeyInfo"]').find("textarea").invoke("val").should("eq", "abcdefghijklmnoprstwxyz123456789");
  });
});
