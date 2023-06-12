import CreateApiKeyCard from "@/components/general/apiKey/CreateApiKeyCard.vue";

describe("Component test for CreateApiKeyCard", () => {
  it("Should have class invalidExpiryTimeText when expire time is invalid", () => {
    cy.mountWithPlugins(CreateApiKeyCard, {
      data() {
        return {
          isExpiryDateValid: false,
          userRoles: ["ROLE_USER", "ROLE_ADMIN"],
        };
      },
    });
    cy.get('label[for="expiryTime"]').should("have.class", "invalidExpiryTimeText");
  });
  it("Should not have class invalidExpiryTimeText when expire time is valid", () => {
    cy.mountWithPlugins(CreateApiKeyCard, {
      data() {
        return {
          isExpiryDateValid: true,
          userRoles: ["ROLE_USER", "ROLE_ADMIN"],
        };
      },
    });
    cy.get('label[for="expiryTime"]').should("not.have.class", "invalidExpiryTimeText");
  });
});
