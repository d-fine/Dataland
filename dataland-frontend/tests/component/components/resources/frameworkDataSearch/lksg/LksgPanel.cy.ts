import LksgPanel from "@/components/resources/frameworkDataSearch/lksg/LksgPanel.vue";
import { LksgData } from "@clients/backend";
import { mount } from "cypress/vue";

describe("Component test for LksgPanel", () => {
  it("Should display the total revenue kpi in the correct format", () => {
    const pseudoLksgData = { social: { general: { dataDate: "2023-01-01", totalRevenue: 1234567.89 } } };
    mount(LksgPanel, {
      data() {
        return {
          waitingForData: false,
          lksgData: [pseudoLksgData as LksgData] as LksgData[],
        };
      },
      created() {
        (this.convertLksgDataToFrontendFormat as ((lksgData: LksgData[]) => void))(this.lksgData);
      },
    });
    cy.get("td:contains('1.23 MM')").should("exist");
  });
});
