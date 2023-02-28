import DatalandFooter from "@/components/general/DatalandFooter.vue";
import { mount } from "@cypress/vue";
import { checkImage } from "../../helper/utilityFunctions";

describe("Component test for WelcomeDataland", () => {
  it("Check if certain logos are present", () => {
    mount(DatalandFooter);
    checkImage("Dataland Logo", "logo_dataland_long.svg");
    cy.get("body").should("contain.text", "Legal");
    cy.get("body").should("contain.text", "Copyright © 2023 Dataland");
    cy.get('[data-cy="imprint"]').should("contain.text", "Imprint").should("have.attr", "to", "/imprint");
    cy.get('[data-cy="data privacy"]').should("contain.text", "Data Privacy").should("have.attr", "to", "/dataprivacy");
  });
});
