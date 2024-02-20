<template>
  <PrimeDialog
    id="successModal"
    :dismissableMask="true"
    :modal="true"
    v-model:visible="dialogIsVisible"
    :closable="false"
    style="border-radius: 0.75rem; text-align: center"
    :show-header="false"
  >
    <div class="text-center" style="display: flex; flex-direction: column">
      <div style="margin: 10px">
        <em class="material-icons info-icon green-text" style="font-size: 2.5em"> check_circle </em>
        <div style="margin: 10px">
          <h2 class="m-0">Success</h2>
        </div>
        <div style="margin: 15px">
          <div>Request closed successfully.</div>
        </div>
        <div style="margin: 10px">
          <PrimeButton label="CLOSE" @click="closeSuccessDialog()" class="p-button-outlined" />
        </div>
      </div>
    </div>
  </PrimeDialog>
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
        :answered-data-requests="answeredDataRequestsForViewPage"
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
import { getAnsweredDataRequestsForViewPage, patchDataRequestStatus } from "@/utils/RequestUtils";
import OverlayPanel from "primevue/overlaypanel";
import { type DataTypeEnum } from "@clients/backend";
import SelectReportingPeriodDialog from "@/components/general/SelectReportingPeriodDialog.vue";
import { type ReportingPeriodTableEntry } from "@/utils/PremadeDropdownDatasets";
import { RequestStatus, type StoredDataRequest } from "@clients/communitymanager";
import PrimeDialog from "primevue/dialog";

export default defineComponent({
  name: "ReviewRequestButtons",
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  components: { PrimeButton, OverlayPanel, SelectReportingPeriodDialog, PrimeDialog },
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
  data() {
    return {
      answeredDataRequestsForViewPage: [] as StoredDataRequest[],
      dialogIsVisible: true,
    };
  },
  mounted() {
    void this.updateAnsweredDataRequestsForViewPage();
  },
  methods: {
    /**
     * Closes the SuccessDialog
     */
    closeSuccessDialog() {
      this.dialogIsVisible = false;
    },
    /**
     * Makes the api call and updates the list of answered data requests.
     */
    async updateAnsweredDataRequestsForViewPage() {
      this.answeredDataRequestsForViewPage = await getAnsweredDataRequestsForViewPage(
        this.companyId,
        this.framework as DataTypeEnum,
        this.getKeycloakPromise,
      );
    },
    /**
     * Method to close the request or provide dropdown for that when the button is clicked
     * @param event ClickEvent
     */
    async closeRequest(event: Event) {
      if (this.mapOfReportingPeriodToActiveDataset.size > 1) {
        this.openReportingPeriodPanel(event);
      } else {
        for (const answeredRequest of this.answeredDataRequestsForViewPage) {
          void (await patchDataRequestStatus(
            answeredRequest.dataRequestId,
            RequestStatus.Closed,
            this.getKeycloakPromise,
          ));
        }
        console.log("submitted the patch for closing of these requests:");
        console.log(this.answeredDataRequestsForViewPage);
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
        for (const answeredRequest of this.answeredDataRequestsForViewPage) {
          await patchDataRequestStatus(answeredRequest.dataRequestId, RequestStatus.Open, this.getKeycloakPromise);
        }
        console.log("submitted the patch for reopening these requests:");
        console.log(this.answeredDataRequestsForViewPage);
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

<style scoped lang="scss"></style>
