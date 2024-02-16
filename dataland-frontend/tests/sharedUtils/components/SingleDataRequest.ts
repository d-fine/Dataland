import { DataTypeEnum } from "@clients/backend";

export const singleDataRequestPage = {
  chooseReportingPeriod2023(year: string): void {
    cy.get('[data-test="reportingPeriods"] div[data-test="toggleChipsFormInput"]')
      .should("exist")
      .get('[data-test="toggle-chip"')
      .contains(year)
      .click()
      .parent()
      .should("have.class", "toggled");
    cy.get("div[data-test='reportingPeriods'] p[data-test='reportingPeriodErrorMessage'").should("not.exist");
  },
  chooseFrameworkLksg(): void {
    const numberOfFrameworks = Object.keys(DataTypeEnum).length;
    cy.get('[data-test="selectFramework"]')
      .should("exist")
      .get('[data-type="select"]')
      .should("exist")
      .click()
      .get('[data-test="datapoint-framework"]')
      .select(DataTypeEnum.Lksg);
    cy.get('[data-test="datapoint-framework"]')
      .children()
      .should("have.length", numberOfFrameworks + 1);
  },
};
