<template>
  <PrimeDialog
    :dismissableMask="true"
    :modal="true"
    v-if="showUpdateRequestDialog"
    v-model:visible="showUpdateRequestDialog"
    :closable="true"
    style="text-align: center"
    :show-header="true"
  >
    <template #header>
      <div class="tab-menu" data-test="updateRequestTabMenu">
        <button :class="{ active: activeTab === 'update request' }" @click="activeTab = 'update request'">
          UPDATE REQUEST
        </button>
        <button
          :class="{ active: activeTab === 'message history' }"
          @click="activeTab = 'message history'"
          v-show="messageHistory.length > 0"
        >
          VIEW HISTORY
        </button>
      </div>
    </template>

    <div v-if="activeTab === 'update request'" data-test="updateRequestModal">
      <EmailDetails :is-optional="true" @has-new-input="updateEmailFields" :show-errors="toggleEmailDetailsError" />
      <PrimeButton
        @click="updateRequest()"
        style="width: 100%; justify-content: center"
        data-test="updateRequestButton"
      >
        <span class="d-letters pl-2" style="text-align: center"> UPDATE REQUEST </span>
      </PrimeButton>
    </div>
    <div v-if="activeTab === 'message history'" data-test="viewHistoryModal">
      <div v-for="message in messageHistory" :key="message.creationTimestamp">
        <div style="color: black; font-weight: bold; font-size: small; text-align: left">
          {{ convertUnixTimeInMsToDateString(message.creationTimestamp) }}
        </div>
        <div class="message">
          <div style="color: black">Sent to: {{ formattedContacts(message.contacts) }}</div>
          <div class="separator" />
          <div style="color: gray">
            {{ message.message }}
          </div>
        </div>
      </div>
    </div>
  </PrimeDialog>
  <PrimeDialog
    v-if="dialogIsVisible"
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
          <h2 class="m-0" data-test="successText">Success</h2>
        </div>
      </div>
    </template>
    <template v-if="!dialogIsSuccess">
      <div class="text-center" style="display: flex; flex-direction: column">
        <div style="margin: 10px">
          <em class="material-icons info-icon red-text" style="font-size: 2.5em"> error </em>
        </div>
        <div style="margin: 10px">
          <h2 class="m-0" data-test="noSuccessText">Changing the status of your request was unsuccessful.</h2>
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
      class="uppercase p-button-outlined p-button p-button-sm d-letters"
      aria-label="RESOLVE REQUEST"
      @click="resolveRequest"
      data-test="resolveRequestButton"
    >
      <span class="px-2">RESOLVE REQUEST</span>
      <span class="material-icons-outlined no-line-height" v-if="mapOfReportingPeriodToActiveDataset.size > 1">
        arrow_drop_down
      </span>
    </PrimeButton>
    <PrimeButton
      class="uppercase p-button p-button-sm d-letters"
      aria-label="REOPEN REQUEST"
      @click="reOpenRequest"
      data-test="reOpenRequestButton"
    >
      <span class="px-2">REOPEN REQUEST</span>
      <span class="material-icons-outlined no-line-height" v-if="mapOfReportingPeriodToActiveDataset.size > 1">
        arrow_drop_down
      </span>
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
import PrimeButton from 'primevue/button';
import { defineComponent, inject, type PropType } from 'vue';
import type Keycloak from 'keycloak-js';
import { getAnsweredDataRequestsForViewPage, patchDataRequest } from '@/utils/RequestUtils';
import OverlayPanel from 'primevue/overlaypanel';
import { type DataMetaInformation, type DataTypeEnum, type ErrorResponse } from '@clients/backend';
import SelectReportingPeriodDialog from '@/components/general/SelectReportingPeriodDialog.vue';
import { ReportingPeriodTableActions, type ReportingPeriodTableEntry } from '@/utils/PremadeDropdownDatasets';
import {
  type AccessStatus,
  type ExtendedStoredDataRequest,
  RequestStatus,
  type StoredDataRequestMessageObject,
} from '@clients/communitymanager';
import PrimeDialog from 'primevue/dialog';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { AxiosError } from 'axios';
import EmailDetails from '@/components/resources/dataRequest/EmailDetails.vue';
import { ApiClientProvider } from '@/services/ApiClients';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';

export default defineComponent({
  name: 'ReviewRequestButtons',
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  components: { EmailDetails, PrimeButton, OverlayPanel, SelectReportingPeriodDialog, PrimeDialog },
  props: {
    companyId: {
      type: String,
      required: true,
    },
    framework: {
      type: String as PropType<DataTypeEnum>,
      required: true,
    },
    mapOfReportingPeriodToActiveDataset: {
      type: Map as PropType<Map<string, DataMetaInformation>>,
      required: true,
    },
  },
  emits: ['isVisible'],
  computed: {
    isVisible() {
      return this.answeredDataRequestsForViewPage.length > 0;
    },
  },
  watch: {
    isVisible(newStatus: boolean) {
      this.$emit('isVisible', newStatus);
    },
    currentChosenDataRequestId: {
      handler(newRequestId: string) {
        if (newRequestId.length === 0) {
          this.messageHistory = [];
        }
        this.fetchMessageHistory().catch((error) => console.error(error));
      },
    },
    mapOfReportingPeriodToActiveDataset() {
      void this.updateAnsweredDataRequestsForViewPage();
    },
  },
  data() {
    return {
      toggleEmailDetailsError: false,
      activeTab: 'update request',
      hasValidEmailForm: false,
      emailContacts: undefined as string[] | undefined,
      emailMessage: undefined as string | undefined,
      showUpdateRequestDialog: false,
      answeredDataRequestsForViewPage: [] as ExtendedStoredDataRequest[],
      dialogIsVisible: false,
      dialog: 'Default\n text.',
      dialogIsSuccess: false,
      actionOnClick: ReportingPeriodTableActions.ReopenRequest,
      currentChosenDataRequestId: '',
      messageHistory: [] as StoredDataRequestMessageObject[],
      currentRunId: 0,
    };
  },
  mounted() {
    void this.updateAnsweredDataRequestsForViewPage();
  },

  methods: {
    convertUnixTimeInMsToDateString,
    /**
     * Method to transform set of string to one string representing the set elements seperated by ','
     * @param contacts set of strings
     * @returns string representing the elements of the set
     */
    formattedContacts(contacts: Set<string>) {
      const contactsList = [...contacts];
      return contactsList.join(', ');
    },
    /**
     * Method to fetch message history with given requestId
     */
    async fetchMessageHistory() {
      try {
        if (this.getKeycloakPromise) {
          const response = await new ApiClientProvider(
            this.getKeycloakPromise()
          ).apiClients.requestController.getDataRequestById(this.currentChosenDataRequestId);
          this.messageHistory = response.data.messageHistory;
        }
      } catch (error) {
        console.error(error);
        this.messageHistory = [];
      }
    },
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
      const runId = ++this.currentRunId;
      const response = await getAnsweredDataRequestsForViewPage(
        this.companyId,
        this.framework,
        Array.from(this.mapOfReportingPeriodToActiveDataset.keys()),
        this.getKeycloakPromise
      );
      if (runId === this.currentRunId) {
        this.answeredDataRequestsForViewPage = response;
      }
    },
    /**
     * Method to close the request or provide dropdown for that when the button is clicked
     * @param event ClickEvent
     */
    async resolveRequest(event: Event) {
      this.actionOnClick = ReportingPeriodTableActions.ResolveRequest;
      if (this.mapOfReportingPeriodToActiveDataset.size > 1) {
        this.openReportingPeriodPanel(event);
      } else {
        for (const answeredRequest of this.answeredDataRequestsForViewPage) {
          await this.patchDataRequest(answeredRequest.dataRequestId, RequestStatus.Resolved);
        }
      }
    },
    /**
     * Method to reopen the request or provide dropdown for that when the button is clicked
     * @param event ClickEvent
     */
    reOpenRequest(event: Event) {
      this.actionOnClick = ReportingPeriodTableActions.ReopenRequest;
      if (this.mapOfReportingPeriodToActiveDataset.size > 1) {
        this.openReportingPeriodPanel(event);
      } else {
        for (const answeredRequest of this.answeredDataRequestsForViewPage) {
          this.currentChosenDataRequestId = answeredRequest.dataRequestId;
          this.showUpdateRequestDialog = true;
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
     * @param accessStatus the access status of a request
     * @param contacts set of email contacts
     * @param message context of the email
     */
    async patchDataRequest(
      dataRequestId: string,
      requestStatusToPatch: RequestStatus,
      accessStatus?: AccessStatus,
      contacts?: string[],
      message?: string
    ) {
      try {
        await patchDataRequest(
          dataRequestId,
          requestStatusToPatch,
          accessStatus,
          // as unknown as Set<string> cast required to ensure proper json is created
          contacts as unknown as Set<string>,
          message,
          undefined,
          this.getKeycloakPromise
        );
      } catch (e) {
        let errorMessage =
          'An unexpected error occurred. Please try again or contact the support team if the issue persists.';
        if (e instanceof AxiosError) {
          const responseMessages = (e.response?.data as ErrorResponse)?.errors;
          errorMessage = responseMessages ? responseMessages[0].message : e.message;
        }
        this.openSuccessModal(errorMessage, false);
        return;
      }
      await this.updateAnsweredDataRequestsForViewPage();
      switch (requestStatusToPatch) {
        case RequestStatus.Open:
          this.openSuccessModal('Request reopened successfully.');
          return;
        case RequestStatus.Resolved:
          this.openSuccessModal('Request resolved successfully.');
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
        this.mapActionToStatus(assertDefined(reportingPeriodTableEntry.actionOnClick))
      );
      if (requestStatusToPatch == RequestStatus.Open) {
        this.currentChosenDataRequestId = dataRequestId;
        this.showUpdateRequestDialog = true;
      } else {
        await this.patchDataRequest(dataRequestId, requestStatusToPatch);
      }
    },
    /**
     * Helper function to handle the different actions given by the different buttons
     * @param actionOnClick the action on click
     * @returns the corresponding request status
     */
    mapActionToStatus(actionOnClick: ReportingPeriodTableActions) {
      switch (actionOnClick) {
        case ReportingPeriodTableActions.ResolveRequest:
          return RequestStatus.Resolved;
        case ReportingPeriodTableActions.ReopenRequest:
          return RequestStatus.Open;
      }
    },
    /**
     * Handles the click on update request
     */
    async updateRequest() {
      if (!this.currentChosenDataRequestId) {
        return;
      }
      if (this.hasValidEmailForm) {
        await this.patchDataRequest(
          this.currentChosenDataRequestId,
          RequestStatus.Open,
          undefined,
          this.emailContacts,
          this.emailMessage
        );
        this.showUpdateRequestDialog = false;
      } else {
        this.toggleEmailDetailsError = !this.toggleEmailDetailsError;
      }
    },
    /**
     * Method to update the email fields
     * @param hasValidForm boolean indicating if the input is correct
     * @param contacts email addresses
     * @param message the content
     */
    updateEmailFields(hasValidForm: boolean, contacts: string[], message: string) {
      this.hasValidEmailForm = hasValidForm;
      this.emailContacts = contacts;
      this.emailMessage = message;
    },
  },
});
</script>

<style scoped lang="scss">
@use '@/assets/scss/variables' as *;

.no-line-height {
  line-height: 0;
}
.tab-menu button {
  border: none;
  background-color: transparent;
  cursor: pointer;
  padding: 10px 20px;
  font-size: 16px;
  font-weight: bold;
  color: black;
  outline: none;
}

.tab-menu button.active {
  border-bottom: 2px solid #e67f3fff;
  color: #e67f3fff;
}
.message {
  width: 100%;
  border: #e0dfde solid 1px;
  padding: $spacing-md;
  border-radius: $radius-xxs;
  text-align: left;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  margin-bottom: 1rem;
  margin-top: 1rem;
}
.separator {
  width: 100%;
  border-bottom: #e0dfde solid 1px;
  margin-top: 1rem;
  margin-bottom: 1rem;
}
</style>
