import { DataTypeEnum } from "@clients/backend";
import { ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE } from "@/utils/Constants";

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
    const numberOfFrameworks = Object.keys(ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE).length;
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
