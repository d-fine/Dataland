import MarketingSection from "@/components/resources/landing/MarketingSection.vue";
import { mount } from "cypress/vue";

describe("Component test for MarketingSection", () => {
  it("Should look correct on mobile", () => {
    mount(MarketingSection, {
      props: {
        isMobile: true,
      },
    });
    cy.get("div[data-test='learn-about-our-vision']").should("not.exist");
    cy.get("div[data-test='marketing-squares']").should("not.exist");
    cy.get("div[data-test='marketing-squares-second']").should("not.exist");
  });
});
