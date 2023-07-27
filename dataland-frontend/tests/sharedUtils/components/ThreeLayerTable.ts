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
