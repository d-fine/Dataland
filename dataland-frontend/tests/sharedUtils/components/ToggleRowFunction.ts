/**
 * Toggles the data-table row group with the given key
 * @param groupKey the key of the row group to expand
 */
export function toggleRowGroup(groupKey: string): void {
  cy.get(`span[id=${groupKey}]`).siblings("button").last().click();
}
