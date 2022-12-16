import CreateApiKeyCard from "@/components/general/apiKey/CreateApiKeyCard.vue";
import { mount } from "cypress/vue";

describe("Component test for CreateApiKeyCard", () => {
  it("Should display proper user role", () => {
    mount(CreateApiKeyCard, {
      props: {
        userRoles: ["ROLE_USER", "ROLE_ADMIN"],
      },
    });
    cy.get("[data-test=userRoles]").should("be.visible");
    cy.get("[data-test=userRoleUser]").should("have.text", "READ");
    cy.get("[data-test=userRoleAdmin]").should("have.text", "WRITE");
  });

  it("Should display only user role", () => {
    mount(CreateApiKeyCard, {
      props: {
        userRoles: ["ROLE_USER"],
      },
    });
    cy.get("[data-test=userRoles]").should("be.visible");
    cy.get("[data-test=userRoleUser]").should("have.text", "READ");
    cy.get("[data-test=userRoleAdmin]").should("not.exist");
  });

  it("Should display only admin role", () => {
    mount(CreateApiKeyCard, {
      props: {
        userRoles: ["ROLE_ADMIN"],
      },
    });
    cy.get("[data-test=userRoles]").should("be.visible");
    cy.get("[data-test=userRoleUser]").should("not.exist");
    cy.get("[data-test=userRoleAdmin]").should("have.text", "WRITE");
  });
});
