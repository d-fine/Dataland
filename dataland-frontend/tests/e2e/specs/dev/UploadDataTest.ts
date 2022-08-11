describe("User interactive tests for Data Upload", () => {
  let companyId: string;
  beforeEach(() => {
    cy.restoreLoginSession("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD"));
  });

  it("cannot create a Company with no input", () => {
    cy.visitAndCheckAppMount("/companies/upload");
    cy.get('button[name="postCompanyData"]').should("be.disabled");
  });

  function uploadCompanyWithEverythingFine(companyName: string) {
    cy.visitAndCheckAppMount("/companies/upload");
    cy.fillCompanyUploadFields(companyName);
    cy.get('button[name="postCompanyData"]').click();
  }

  it("Create a Company when everything is fine", () => {
    const companyName = "Test company";
    uploadCompanyWithEverythingFine(companyName);
    cy.get("body").should("contain", "success");
    cy.get("span[title=companyId]").then(($companyID) => {
      companyId = $companyID.text();
      cy.visitAndCheckAppMount(`/companies/${companyId}`);
      cy.get("body").should("contain", companyName);
    });
  });

  it("Create a Company with insufficient rights should fail", () => {
    cy.restoreLoginSession();
    const companyName = "Test company";
    uploadCompanyWithEverythingFine(companyName);
    cy.get("body").should("contain", "Sorry");
  });

  function uploadEuTaxonomyDatasetForNonFinancialsWithReportingObligation() {
    cy.visitAndCheckAppMount("/companies/${companyId}/frameworks/eutaxonomy-non-financials/upload");
    cy.get('button[name="postEUData"]', { timeout: 2 * 1000 }).should("be.visible");
    cy.get('input[name="companyId"]').type(companyId, { force: true });
    cy.get('input[name="Reporting Obligation"][value=Yes]').check({ force: true });
    cy.get('select[name="Attestation"]').select("None");
    for (const argument of ["capex", "opex"]) {
      cy.get(`div[title=${argument}] input`).each(($element, index) => {
        const inputNumber = 10 * index + 7;
        cy.wrap($element).type(inputNumber.toString(), { force: true });
      });
    }
    cy.get("div[title=revenue] input").eq(0).type("0");
    cy.get("div[title=revenue] input").eq(1).type("0");
    cy.get('button[name="postEUData"]', { timeout: 2 * 1000 }).should("not.be.disabled");
    cy.get('button[name="postEUData"]').click({ force: true });
  }

  it("Create EU Taxonomy Dataset with Reporting Obligation and Check the Link", () => {
    uploadEuTaxonomyDatasetForNonFinancialsWithReportingObligation();
    cy.get("body").should("contain", "success").should("contain", "EU Taxonomy Data");
    cy.get("span[title=dataId]").then(() => {
      cy.get("span[title=companyId]").then(($companyID) => {
        const companyID = $companyID.text();
        cy.intercept("/api/data/eutaxonomy/nonfinancials/*").as("retrieveTaxonomyData");
        cy.visitAndCheckAppMount(`/companies/${companyID}/frameworks/eutaxonomy`);
      });
      cy.wait("@retrieveTaxonomyData", { timeout: 120 * 1000 })
        .get("body")
        .should("contain", "Eligible Revenue")
        .should("not.contain", "No data has been reported");
    });
  });

  it("Create EU Taxonomy Dataset with Reporting Obligation and insufficient rights should fail", () => {
    cy.restoreLoginSession();
    uploadEuTaxonomyDatasetForNonFinancialsWithReportingObligation();
    cy.get("body").should("contain", "Sorry");
  });
});
