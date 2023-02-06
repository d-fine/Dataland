import LandingLogin from "@/components/resources/landing/LandingLogin.vue";
import { mount } from "cypress/vue";

describe("Component test for LandingLogin", () => {
  it("Should look correct on mobile", () => {
    mount(LandingLogin, {
      props: {
        isMobile: true,
      },
    });
    cy.get("div[data-test='landing-page-top-info-message']").should("not.exist");
    cy.get("div[data-test='landing-page-top-logo']").should("have.class", "mobile-logo");
    cy.get("div[data-test='landing-page-login-button']").should("not.exist");
    cy.get("div[data-test='landing-page-mobile-description']").should("exist");
    cy.get("div[data-test='landing-page-graphic-vision']").should("have.class", "isMobile");
    cy.get("div[data-test='landing-page-graphic-vision'] h1").should("not.exist");
    cy.get("div[data-test='landing-page-graphic-vision-img'] ").should("have.attr", "class", "");
    cy.get("div[data-test='landing-page-create-account']").should("not.exist");
  });
});
