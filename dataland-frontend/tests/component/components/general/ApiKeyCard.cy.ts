import ApiKeyCard from "@/components/general/apiKey/ApiKeyCard.vue";
import { mount } from "cypress/vue";

describe("Component test for ApiKeyCard", () => {
  it("Should contain text 'The API Key expired' when Api Key is expired", () => {
    mount(ApiKeyCard, {
      data() {
        return {
          viewDeleteConfirmation: false,
        };
      },
      props: {
        userRoles: ["ROLE_USER", "ROLE_ADMIN"],
        expiryDateInMilliseconds: 1,
      },
    });
    cy.get("div#existingApiKeyCard").should("exist").should("contain.text", "The API Key expired");
    cy.get("div#existingApiKeyCard span").should("have.class", "text-red-700");
  });
  it("Should contain text 'The API Key has no defined expiry date' when Api Key has no defined expiry date", () => {
    mount(ApiKeyCard, {
      data() {
        return {
          viewDeleteConfirmation: false,
        };
      },
      props: {
        userRoles: ["ROLE_USER", "ROLE_ADMIN"],
        expiryDateInMilliseconds: null,
      },
    });
    cy.get("div#existingApiKeyCard").should("exist").should("contain.text", "The API Key has no defined expiry date");
  });
});
