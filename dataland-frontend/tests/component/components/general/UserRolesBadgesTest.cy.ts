import UserRolesBadges from "@/components/general/apiKey/UserRolesBadges.vue";
import { mount } from "cypress/vue";

describe("Component test for UserRolesBadges", () => {
  it("Should display proper user roles", () => {
    mount(UserRolesBadges, {
      props: {
        userRoles: ["ROLE_USER", "ROLE_UPLOADER"],
      },
    });
    cy.get("[data-test=userRoleUser]").should("have.text", "READ");
    cy.get("[data-test=userRoleUploader]").should("have.text", "WRITE");
    cy.get("[data-test=userRoleAdmin]").should("not.exist");
  });

  it("Should display only user role", () => {
    mount(UserRolesBadges, {
      props: {
        userRoles: ["ROLE_USER"],
      },
    });
    cy.get("[data-test=userRoleUser]").should("have.text", "READ");
    cy.get("[data-test=userRoleAdmin]").should("not.exist");
  });

  it("Should display only admin role", () => {
    mount(UserRolesBadges, {
      props: {
        userRoles: ["ROLE_ADMIN"],
      },
    });
    cy.get("[data-test=userRoleUser]").should("not.exist");
    cy.get("[data-test=userRoleUploader]").should("not.exist");
    cy.get("[data-test=userRoleAdmin]").should("have.text", "ADMIN");
  });
});
