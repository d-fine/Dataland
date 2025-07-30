import { FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import { selectItemFromDropdownByValue } from '@sharedUtils/Dropdown';

export const singleDataRequestPage = {
  chooseReportingPeriod(reportingPeriod: string = '2023'): void {
    cy.get('[data-test="reportingPeriods"] div[data-test="toggleChipsFormInput"]').should('exist');
    cy.get('[data-test="toggle-chip"]').contains(reportingPeriod).click();
    cy.get('[data-test="toggle-chip"]').contains(reportingPeriod).parent().should('have.class', 'toggled');
    cy.get("div[data-test='reportingPeriods'] p[data-test='reportingPeriodErrorMessage']").should('not.exist');
  },
  chooseFrameworkLksg(): void {
    const numberOfFrameworks = Object.keys(FRAMEWORKS_WITH_VIEW_PAGE).length;
    selectItemFromDropdownByValue(cy.get('[data-test="datapoint-framework"]'), 'LkSG', true);
    cy.get('[data-test="datapoint-framework"]').get('.p-select-dropdown').click();
    cy.get('.p-select-list').find('li').should('have.length', numberOfFrameworks);
    cy.get('[data-test="datapoint-framework"]').get('.p-select-dropdown').click();
  },
};
