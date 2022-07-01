describe("Data Upload Page", function () {
  beforeEach(() => {
    cy.restoreLoginSession();
  });
  it("page should be present", function () {
    cy.visit("/upload");
    cy.get("#app").should("exist");
    cy.get(".p-card-title").should("contain", "Create a Company");
    const inputValue = "A company name";
    cy.get('button[name="postCompanyData"]').contains("Post Company").should("be.disabled");
    cy.get("input[name=companyName]").type(inputValue, { force: true });
    cy.get('button[name="postCompanyData"]').should("be.disabled");
    cy.get("input[name=headquarters]").type("applications", { force: true });
    cy.get('button[name="postCompanyData"]').should("be.disabled");
    cy.get("input[name=sector]").type("Handmade", { force: true });
    cy.get('button[name="postCompanyData"]').should("be.disabled");
    cy.get("input[name=marketCap]").type("123", { force: true });
    cy.get('button[name="postCompanyData"]').should("be.disabled");
    cy.get("input[name=reportingDateOfMarketCap]").type("2021-09-02", {
      force: true,
    });
    cy.get('button[name="postCompanyData"]').should("be.disabled");
    cy.get("select[name=identifierType]").select("ISIN");
    cy.get('button[name="postCompanyData"]').should("be.disabled");
    cy.get("input[name=identifierValue]").type("IsinValueId", { force: true });
    cy.get('button[name="postCompanyData"]').should("not.be.disabled");
  });
});
