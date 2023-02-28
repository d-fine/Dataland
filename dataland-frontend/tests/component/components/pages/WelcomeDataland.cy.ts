import WelcomeDataland from "@/components/pages/WelcomeDataland.vue";
import { mount } from "@cypress/vue";
import { checkButton, checkImage } from "../../helper/utilityFunctions";

describe("Component test for WelcomeDataland", () => {
  beforeEach(() => {
    mount(WelcomeDataland, { propsData: { isMobile: false } });
  });

  it("Check if certain logos are present", () => {
    checkImage("Dataland image logo", "bg_graphic_vision.svg");
    checkImage("Dataland banner logo", "logo_dataland_long.svg");
    checkImage("pwc", "pwc.svg");
    checkImage("d-fine GmbH", "dfine.svg");
  });

  it("Check if certain messages and buttons are present", () => {
    cy.get('[data-cy="banner message"]').should("contain.text", "THE ALTERNATIVE TO DATA MONOPOLIES");
    checkButton("join_dataland_button", "Create a preview account");
    checkButton("eu_taxonomy_sample_button", "EU Taxonomy sample data");
    checkButton("login_dataland_button", "Login to preview account");
  });
});
