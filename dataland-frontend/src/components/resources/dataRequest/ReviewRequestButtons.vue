<template>
  <div v-if="isVisible">
    <PrimeButton
      class="uppercase p-button-outlined p-button p-button-sm d-letters ml-3"
      aria-label="CLOSE REQUEST"
      @click="closeRequest"
      data-test="closeRequestButton"
    >
      <span class="px-2">CLOSE REQUEST</span>
      <span class="material-icons-outlined" v-if="mapOfReportingPeriodToActiveDataset.size > 1"> arrow_drop_down </span>
    </PrimeButton>
    <PrimeButton
      class="uppercase p-button p-button-sm d-letters ml-3"
      aria-label="REOPEN REQUEST"
      @click="reOpenRequest"
      data-test="reOpenRequestButton"
    >
      <span class="px-2">REOPEN REQUEST</span>
      <span class="material-icons-outlined" v-if="mapOfReportingPeriodToActiveDataset.size > 1"> arrow_drop_down </span>
    </PrimeButton>
    <OverlayPanel ref="reportingPeriodsOverlayPanel">
      <SelectReportingPeriodDialog
        :mapOfReportingPeriodToActiveDataset="mapOfReportingPeriodToActiveDataset"
        @selected-reporting-period="handleReportingPeriodSelection"
      />
    </OverlayPanel>
  </div>
</template>

<script lang="ts">
import PrimeButton from "primevue/button";
import { defineComponent } from "vue";
import { inject } from "vue";
import type Keycloak from "keycloak-js";
import { getAnsweredDataRequestsForViewPage } from "@/utils/RequestUtils";
import OverlayPanel from "primevue/overlaypanel";
import { type DataTypeEnum } from "@clients/backend";
import SelectReportingPeriodDialog from "@/components/general/SelectReportingPeriodDialog.vue";
import { type ReportingPeriodTableEntry } from "@/utils/PremadeDropdownDatasets";

export default defineComponent({
  name: "ReviewRequestButtons",
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  components: { PrimeButton, OverlayPanel, SelectReportingPeriodDialog },
  props: {
    isVisible: {
      default: false,
    },
    companyId: {
      type: String,
      required: true,
    },
    framework: {
      type: String,
      required: true,
    },
    mapOfReportingPeriodToActiveDataset: {
      type: Map,
      required: true,
    },
  },
  methods: {
    /**
     * Method to close the request or provide dropdown for that when the button is clicked
     * @param event ClickEvent
     */
    async closeRequest(event: Event) {
      if (this.mapOfReportingPeriodToActiveDataset.size > 1) {
        this.openReportingPeriodPanel(event);
      } else {
        console.log(" here I will close the request #todo");
        const listOFMyRequests = await getAnsweredDataRequestsForViewPage(
          this.companyId,
          this.framework as DataTypeEnum,
          this.getKeycloakPromise,
        );
        console.log(listOFMyRequests);
      }
    },
    /**
     * Method to reopen the request or provide dropdown for that when the button is clicked
     * @param event ClickEvent
     */
    async reOpenRequest(event: Event) {
      if (this.mapOfReportingPeriodToActiveDataset.size > 1) {
        this.openReportingPeriodPanel(event);
      } else {
        console.log(" here I will reopen the request #todo");
        const listOFMyRequests = await getAnsweredDataRequestsForViewPage(
          this.companyId,
          this.framework as DataTypeEnum,
          this.getKeycloakPromise,
        );
        console.log(listOFMyRequests);
      }
    },
    /**
     * Opens Overlay Panel for selecting a reporting period to review request for
     * @param event event
     */
    openReportingPeriodPanel(event: Event) {
      const panel = this.$refs.reportingPeriodsOverlayPanel as OverlayPanel;
      if (panel) {
        panel.toggle(event);
      }
    },
    /**
     * Handles the selection of the reporting period in th dropdown panel
     * @param reportingPeriodTableEntry object, which was chosen
     */
    handleReportingPeriodSelection(reportingPeriodTableEntry: ReportingPeriodTableEntry) {
      console.log("this is the selected Reporting period:");
      console.log(reportingPeriodTableEntry.reportingPeriod);
    },
  },
});
</script>
