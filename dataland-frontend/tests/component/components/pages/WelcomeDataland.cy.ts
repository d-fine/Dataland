import WelcomeDataland from "@/components/pages/WelcomeDataland.vue";
import { mount } from "cypress/vue";
import { checkButton, checkImage, checkFooter } from "@ct/helper/utilityFunctions";

describe("Component test for WelcomeDataland", () => {
  it("Check if essential elements are present", () => {
    mount(WelcomeDataland, { propsData: { isMobile: false } });
    cy.get('[data-test="banner message"]').should("contain.text", "THE ALTERNATIVE TO DATA MONOPOLIES");
    checkImage("Dataland image logo", "bg_graphic_vision.svg");
    checkImage("Dataland banner logo", "logo_dataland_long.svg");
    checkImage("pwc", "pwc.svg");
    checkImage("d-fine GmbH", "dfine.svg");
    checkButton("join_dataland_button", "Create a preview account");
    checkButton("eu_taxonomy_sample_button", "EU Taxonomy sample data");
    checkButton("login_dataland_button", "Login to preview account");
    checkFooter();
  });
});
