export const threeLayerTable = {
  getCategorySelector(categoryLabel: string): string {
    return `span.p-badge:contains('${categoryLabel}')`;
  },
  getSubcategorySelector(subcategoryLabel: string): string {
    return `tr.p-rowgroup-header span:contains('${subcategoryLabel}')`;
  },
  getFieldByContentSelector(partialContent: string): string {
    return `td[role='cell'] > span:contains('${partialContent}')`;
  },
  getFieldByTestIdentifier(dataTest: string, dataColumnIndex = 0): Cypress.Chainable {
    return cy.get(`td span[data-test='${dataTest}']`).parent().siblings().eq(dataColumnIndex);
  },
  toggleCategory(categoryLabel: string): void {
    cy.get(this.getCategorySelector(categoryLabel)).click();
  },
  toggleSubcategory(subcategoryLabel: string): void {
    cy.get(this.getSubcategorySelector(subcategoryLabel)).click();
  },
  subcategoryIsNotVisible(partialContent: string): void {
    cy.get(this.getSubcategorySelector(partialContent)).should("not.visible");
  },
};
