import { checkViewButtonWorks, verifyCompanySearchResultTable } from "../../../support/commands";

describe("Data Search Page Skyminder", function () {
  beforeEach(() => {
    cy.restoreLoginSession();
  });
  it("page should be present", function () {
    cy.visitAndCheckAppMount("/search");
    cy.get(".p-card-title").should("contain", "Skyminder Data Search");
    const inputValueCountry = "A 3 letter country code";
    cy.get("input[name=code]")
      .should("not.be.disabled")
      .click({ force: true })
      .type(inputValueCountry)
      .should("have.value", inputValueCountry);
    const inputValueCompany = "A company name";
    cy.get("input[name=name]")
      .should("not.be.disabled")
      .click({ force: true })
      .type(inputValueCompany)
      .should("have.value", inputValueCompany);
    cy.get("button.p-button").contains("Clear").should("not.be.disabled").click();
    cy.get("input[name=code]").should("have.value", "");
    cy.get("input[name=name]").should("have.value", "");
    cy.get('button[name="getSkyminderData"]').contains("Get Skyminder Data").should("not.be.disabled");
  });
});

describe("Data Search Page Company", function () {
  beforeEach(() => {
    cy.restoreLoginSession();
  });
  it("page should be present", function () {
    cy.visitAndCheckAppMount("/search");
    cy.get(".p-card-title").should("contain", "Company Search");
    const inputValue = "d-fine";
    cy.get("input[name=companyName]")
      .should("not.be.disabled")
      .click({ force: true })
      .type(inputValue)
      .should("have.value", inputValue);
    cy.get("button[name=show_all_companies_button].p-button")
      .should("not.be.disabled")
      .should("contain", "Show all companies")
      .click({ force: true });
    verifyCompanySearchResultTable();
    checkViewButtonWorks();
  });
});
