import DatalandFooter from "@/components/general/DatalandFooter.vue";
import { mount } from "cypress/vue";
import { checkImage } from "@ct/testUtils/existenceChecks";

describe("Component test for DatalandFooter", () => {
  it("Check if footer is as expected", () => {
    mount(DatalandFooter);
    checkImage("Dataland Logo", "logo_dataland_long.svg");
    cy.get("body").should("contain.text", "Legal");
    cy.get("body").should("contain.text", "Copyright © 2023 Dataland");
    cy.get('[data-test="imprint"]').should("contain.text", "Imprint").should("have.attr", "to", "/imprint");
    cy.get('[data-test="data privacy"]')
      .should("contain.text", "Data Privacy")
      .should("have.attr", "to", "/dataprivacy");
  });
});
