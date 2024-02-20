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
    <template v-if="dialogIsSuccess">
      <div class="text-center" style="display: flex; flex-direction: column">
        <div style="margin: 10px">
          <em class="material-icons info-icon green-text" style="font-size: 2.5em"> check_circle </em>
        </div>
        <div style="margin: 10px">
          <h2 class="m-0">Success</h2>
        </div>
      </div>
    </template>
    <template v-if="!dialogIsSuccess">
      <div class="text-center" style="display: flex; flex-direction: column">
        <div style="margin: 10px">
          <em class="material-icons info-icon red-text" style="font-size: 2.5em"> error </em>
        </div>
        <div style="margin: 10px">
          <h2 class="m-0">Changing the status of your request was unsuccessful.</h2>
        </div>
      </div>
    </template>

    <div class="text-block" style="margin: 15px; white-space: pre">
      {{ dialog }}
    </div>
    <div style="margin: 10px">
      <PrimeButton label="CLOSE" @click="closeSuccessModal()" class="p-button-outlined" />
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
        :action-on-click="actionOnClick"
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
import { type DataTypeEnum, type ErrorResponse } from "@clients/backend";
import SelectReportingPeriodDialog from "@/components/general/SelectReportingPeriodDialog.vue";
import { ReportingPeriodTableActions, type ReportingPeriodTableEntry } from "@/utils/PremadeDropdownDatasets";
import { RequestStatus, type StoredDataRequest } from "@clients/communitymanager";
import PrimeDialog from "primevue/dialog";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { AxiosError } from "axios";

export default defineComponent({
  name: "ReviewRequestButtons",
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  components: { PrimeButton, OverlayPanel, SelectReportingPeriodDialog, PrimeDialog },
  props: {
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
  emits: ["isVisible"],
  computed: {
    isVisible() {
      return this.answeredDataRequestsForViewPage.length > 0;
    },
  },
  watch: {
    isVisible(newStatus: boolean) {
      this.$emit("isVisible", newStatus);
    },
    companyId() {
      void this.updateAnsweredDataRequestsForViewPage();
    },
  },
  data() {
    return {
      answeredDataRequestsForViewPage: [] as StoredDataRequest[],
      dialogIsVisible: false,
      dialog: "Default\n text.",
      dialogIsSuccess: false,
      actionOnClick: ReportingPeriodTableActions.ReopenRequest,
    };
  },
  mounted() {
    void this.updateAnsweredDataRequestsForViewPage();
  },
  methods: {
    /**
     * Closes the SuccessModal
     */
    closeSuccessModal() {
      this.dialogIsVisible = false;
    },
    /**
     * Opens the SuccessModal with given dialog
     * @param dialog desired dialog
     * @param dialogIsSuccess if false, display error message
     */
    openSuccessModal(dialog: string, dialogIsSuccess: boolean = true) {
      this.dialogIsSuccess = dialogIsSuccess;
      this.dialog = dialog;
      this.dialogIsVisible = true;
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
      this.actionOnClick = ReportingPeriodTableActions.CloseRequest;
      if (this.mapOfReportingPeriodToActiveDataset.size > 1) {
        this.openReportingPeriodPanel(event);
      } else {
        for (const answeredRequest of this.answeredDataRequestsForViewPage) {
          await this.patchDataRequestStatus(answeredRequest.dataRequestId, RequestStatus.Closed);
        }
      }
    },
    /**
     * Method to reopen the request or provide dropdown for that when the button is clicked
     * @param event ClickEvent
     */
    async reOpenRequest(event: Event) {
      this.actionOnClick = ReportingPeriodTableActions.ReopenRequest;
      if (this.mapOfReportingPeriodToActiveDataset.size > 1) {
        this.openReportingPeriodPanel(event);
      } else {
        for (const answeredRequest of this.answeredDataRequestsForViewPage) {
          await this.patchDataRequestStatus(answeredRequest.dataRequestId, RequestStatus.Open);
        }
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
     * Trys to patch DataRequest, displays possible error message
     * @param dataRequestId DataRequest to be closed
     * @param requestStatusToPatch desired requestStatus
     */
    async patchDataRequestStatus(dataRequestId: string, requestStatusToPatch: RequestStatus) {
      try {
        void (await patchDataRequestStatus(dataRequestId, requestStatusToPatch, this.getKeycloakPromise));
      } catch (e) {
        let errorMessage =
          "An unexpected error occurred. Please try again or contact the support team if the issue persists.";
        if (e instanceof AxiosError) {
          const responseMessages = (e.response?.data as ErrorResponse)?.errors;
          errorMessage += responseMessages ? responseMessages[0].message : e.message;
        }
        this.openSuccessModal(errorMessage, false);
        return;
      }
      await this.updateAnsweredDataRequestsForViewPage();
      switch (requestStatusToPatch) {
        case RequestStatus.Open:
          this.openSuccessModal("Request opened successfully.");
          return;
        case RequestStatus.Closed:
          this.openSuccessModal("Request closed successfully.");
          return;
      }
    },
    /**
     * Handles the selection of the reporting period in th dropdown panel
     * @param reportingPeriodTableEntry object, which was chosen
     */
    async handleReportingPeriodSelection(reportingPeriodTableEntry: ReportingPeriodTableEntry) {
      const dataRequestId = assertDefined(reportingPeriodTableEntry.dataRequestId);
      const requestStatusToPatch = assertDefined(
        this.mapActionToStatus(assertDefined(reportingPeriodTableEntry.actionOnClick)),
      );
      await this.patchDataRequestStatus(dataRequestId, requestStatusToPatch);
    },
    /**
     * Helper function to handle the different actions given by the different buttons
     * @param actionOnClick the action on click
     * @returns the corresponding request status
     */
    mapActionToStatus(actionOnClick: ReportingPeriodTableActions) {
      switch (actionOnClick) {
        case ReportingPeriodTableActions.CloseRequest:
          return RequestStatus.Closed;
        case ReportingPeriodTableActions.ReopenRequest:
          return RequestStatus.Open;
      }
    },
  },
});
</script>

<style scoped lang="scss"></style>
