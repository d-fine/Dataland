describe("EU Taxonomy Data", () => {
  it("Check for a dataset, that bears decent data, that the data is present and displayed", () => {
    cy.restoreLoginSession();
    cy.retrieveDataIdsList().then((dataIdList: Array<string>) => {
      cy.intercept("**/api/data/eutaxonomies/*").as("retrieveTaxonomyData");
      cy.visit("/data/eutaxonomies/" + dataIdList[0]);
      cy.wait("@retrieveTaxonomyData", { timeout: 60 * 1000 }).then(() => {
        cy.get("h3", { timeout: 90 * 1000 }).should("be.visible");
        cy.get("h3").contains("Revenue");
        cy.get("h3").contains("CapEx");
        cy.get("h3").contains("OpEx");
        cy.get(".d-card").should("contain", "Eligible");
        cy.get(".d-card .p-progressbar").should("exist");
      });
    });
  });
});
