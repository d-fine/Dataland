describe("As a developer I want to ensure that the SkyMinder integration works fine", () => {
  beforeEach(() => {
    cy.ensureLoggedIn();
    cy.visitAndCheckAppMount("/companies-only-search");
  });

  it("page should be present", function () {
    cy.visitAndCheckAppMount("/companies-only-search");
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

  it("Should display an error when I make a Skyminder search with no input", () => {
    cy.get('button[name="getSkyminderData"]').click();
    cy.get("body").should("contain", "Sorry");
  });

  it("Should display results when I make a valid Skyminder search", () => {
    cy.get("input[name=code]").type("DEU", { force: true });
    cy.get("input[name=name]").type("BMW", { force: true });
    cy.get('button[name="getSkyminderData"]').click();
    cy.get("table", { timeout: 30 * 1000 }).should("exist");
  });
});
