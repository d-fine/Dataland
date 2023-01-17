import CreateApiKeyCard from "@/components/general/apiKey/CreateApiKeyCard.vue";
import { mount } from "cypress/vue";

describe("Component test for CreateApiKeyCard", () => {
  it("Should have class invalidExpiryTimeText when expire time is invalid", () => {
    mount(CreateApiKeyCard, {
      data() {
        return {
          isExpiryDateValid: false,
        };
      },
      props: {
        userRoles: ["ROLE_USER", "ROLE_ADMIN"],
      },
    });
    cy.get('label[for="expiryTime"]').should("have.class", "invalidExpiryTimeText");
  });
  it("Should not have class invalidExpiryTimeText when expire time is valid", () => {
    mount(CreateApiKeyCard, {
      data() {
        return {
          isExpiryDateValid: true,
        };
      },
      props: {
        userRoles: ["ROLE_USER", "ROLE_ADMIN"],
      },
    });
    cy.get('label[for="expiryTime"]').should("not.have.class", "invalidExpiryTimeText");
  });
});
