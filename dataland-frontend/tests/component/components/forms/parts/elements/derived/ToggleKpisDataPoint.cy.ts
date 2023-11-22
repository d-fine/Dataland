import DataPointFormWithToggle from "@/components/forms/parts/kpiSelection/DataPointFormWithToggle.vue";
import {
  euTaxonomyKpiInfoMappings,
  euTaxonomyKpiNameMappings,
} from "@/components/forms/parts/kpiSelection/EuTaxonomyKPIsModel";
describe("Component tests for toggle data point", () => {
  it("On the upload page, ensure that data point can be hidden and shown and the data will be assigned accordingly", () => {
    cy.mountWithPlugins(DataPointFormWithToggle, {
      data() {
        return {
          dataPointIsAvailable: true,
          currentAmountValue: "",
          currentPercentageValue: "",
          currentReportValue: "",
          currentPageValue: "",
          currentQualityValue: "",
          amountValueBeforeDataPointWasDisabled: "",
          percentageValueBeforeDataPointWasDisabled: "",
          reportValueBeforeDataPointWasDisabled: "",
          pageValueBeforeDataPointWasDisabled: "",
          qualityValueBeforeDataPointWasDisabled: "",
        };
      },
      props: {
        name: "tradingPortfolioInPercent",
        kpiInfoMappings: euTaxonomyKpiInfoMappings,
        kpiNameMappings: euTaxonomyKpiNameMappings,
      },
    }).then((mounted) => {
      cy.get('[data-test="valueAsPercentage"]').should("be.visible").type("133");
      cy.get('[data-test="valueAsPercentageInSecondInputMode"]').should("not.exist");
      cy.get('[data-test="qualityValue"]').select("Estimated");
      cy.wrap(mounted.component).its("currentQualityValue").should("eq", "Estimated");
      cy.get('[data-test="dataPointToggleButton"]').click();
      cy.get('[data-test="valueAsPercentage"]').should("not.be.visible");
      cy.get('[data-test="qualityValue"]').should("not.be.visible");
      cy.wrap(mounted.component).its("currentValue").should("eq", "");
      cy.wrap(mounted.component).its("currentQualityValue").should("eq", "NA");
      cy.get('[data-test="dataPointToggleButton"]').click();
      cy.get('[data-test="valueAsPercentage"]').should("be.visible");
      cy.get('[data-test="qualityValue"]').should("be.visible");
      cy.wrap(mounted.component).its("currentValue").should("eq", "133");
      cy.wrap(mounted.component).its("currentQualityValue").should("eq", "Estimated");
    });
  });
});
