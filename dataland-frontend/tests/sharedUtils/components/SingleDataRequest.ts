import { DataTypeEnum } from "@clients/backend";
import { selectItemFromDropdownByValue } from "@sharedUtils/Dropdown";

export const singleDataRequestPage = {
  chooseReportingPeriod(reportingPeriod: string = "2023"): void {
    cy.get('[data-test="reportingPeriods"] div[data-test="toggleChipsFormInput"]')
      .should("exist")
      .get('[data-test="toggle-chip"')
      .contains(reportingPeriod)
      .click()
      .parent()
      .should("have.class", "toggled");
    cy.get("div[data-test='reportingPeriods'] p[data-test='reportingPeriodErrorMessage'").should("not.exist");
  },
  chooseFrameworkLksg(): void {
    const numberOfFrameworks = Object.keys(DataTypeEnum).length;
    selectItemFromDropdownByValue(
      cy.get('[data-test="selectFramework"]').should("exist").get('[data-test="datapoint-framework"]'),
      "LkSG",
      true,
    );
    cy.get('[data-test="datapoint-framework"]').get(".p-dropdown-trigger").click();
    cy.get(".p-dropdown-items").find("li").should("have.length", numberOfFrameworks);
    cy.get('[data-test="datapoint-framework"]').get(".p-dropdown-trigger").click();
  },
};
