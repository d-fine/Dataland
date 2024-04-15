<template>
  <AuthenticationWrapper>
    <PrimeDialog
      :dismissableMask="true"
      :modal="true"
      v-model:visible="showNewMessageDialog"
      :closable="true"
      style="text-align: center"
      :show-header="true"
    >
      <template #header>
        <span style="font-weight: bold; margin-right: auto">NEW MESSAGE</span>
      </template>
      <EmailDetails :is-optional="false" @has-valid-input="updateEmailFields" />
      <PrimeButton @click="addMessage()" style="width: 100%; justify-content: center">
        <span class="d-letters pl-2" style="text-align: center"> SEND MESSAGE </span>
      </PrimeButton>
    </PrimeDialog>
    <TheHeader />
    <div class="sheet">
      <div class="headline">
        <BackButton />
      </div>
      <h1 class="text-left">Data Request</h1>
    </div>
    <div class="py-4 paper-section">
      <div class="grid col-9 justify-content-around">
        <div class="col-4">
          <div class="card">
            <div class="card__title">Request Details</div>
            <div class="card__separator" />
            <div class="card__subtitle">Company</div>
            <div class="card__data">{{ companyName }}</div>
            <div class="card__subtitle">Framework</div>
            <div class="card__data">
              {{ getFrameworkTitle(storedDataRequest.dataType) }}

              <div
                v-if="frameworkHasSubTitle(storedDataRequest.dataType)"
                style="color: gray; font-size: smaller; line-height: 0.5; white-space: nowrap"
              >
                <br />
                {{ getFrameworkSubtitle(storedDataRequest.dataType) }}
              </div>
            </div>
            <div class="card__subtitle">Reporting year</div>
            <div class="card__data">{{ storedDataRequest.reportingPeriod }}</div>
          </div>
          <div
            v-if="isDatasetAvailable()"
            class="link claim-panel-text"
            style="font-weight: bold"
            @click="goToResolveDataRequestPage()"
          >
            VIEW DATASET
          </div>
        </div>
        <div class="grid col-8 flex-direction-column">
          <div class="col-12">
            <div class="card">
              <span style="display: flex; align-items: center">
                <div class="card__title">Request is:</div>
                <div :class="badgeClass(storedDataRequest.requestStatus)" style="display: inline-flex">
                  {{ storedDataRequest.requestStatus }}
                </div>
                <div class="card__subtitle">
                  since {{ convertUnixTimeInMsToDateString(storedDataRequest.lastModifiedDate) }}
                </div>
                <div style="margin-left: auto">
                  <PrimeButton v-if="isRequestStatusAnswered()" @click="goToResolveDataRequestPage()">
                    <span class="d-letters pl-2"> Resolve Request </span>
                  </PrimeButton>
                </div>
              </span>
            </div>
            <div class="card">
              <span style="display: flex; align-items: center">
                <div class="card__title" style="margin-right: auto">Provided Contact Details & Messages</div>
                <div
                  v-if="allowNewMessage()"
                  style="cursor: pointer; display: flex; align-items: center"
                  @click="openMessageDialog()"
                >
                  <i class="pi pi-file-edit pl-3 pr-3" aria-hidden="true" />
                  <div style="font-weight: bold">NEW MESSAGE</div>
                </div>
              </span>
              <div class="card__separator" />
              <div v-for="message in storedDataRequest.messageHistory" :key="message.creationTimestamp">
                <div style="color: black; font-weight: bold; font-size: small">
                  {{ convertUnixTimeInMsToDateString(message.creationTimestamp) }}
                </div>
                <div class="message">
                  <div style="color: black">Sent to: {{ formattedContacts(message.contacts) }}</div>
                  <div class="card__separator" />
                  <div style="color: gray">
                    {{ message.message }}
                  </div>
                </div>
              </div>
            </div>
            <div class="card" v-if="isWithdrawAble()">
              <div class="card__title">Withdraw Request</div>
              <div class="card__separator" />
              <div>
                Once a data request is withdrawn, it will be removed from your data request list. The data owner will
                not be notified anymore.
                <a
                  class="link"
                  style="display: inline-flex; font-weight: bold; color: black"
                  @click="withdrawRequest()"
                >
                  Withdraw request.</a
                >
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <TheFooter :is-light-version="true" />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import BackButton from "@/components/general/BackButton.vue";
import TheFooter from "@/components/generics/TheFooter.vue";
import { ApiClientProvider } from "@/services/ApiClients";
import { RequestStatus, type StoredDataRequest } from "@clients/communitymanager";
import type Keycloak from "keycloak-js";
import { frameworkHasSubTitle, getFrameworkSubtitle, getFrameworkTitle } from "@/utils/StringFormatter";
import { badgeClass, patchDataRequestStatus } from "@/utils/RequestUtils";
import { convertUnixTimeInMsToDateString } from "@/utils/DataFormatUtils";
import PrimeButton from "primevue/button";
import PrimeDialog from "primevue/dialog";
import EmailDetails from "@/components/resources/dataRequest/EmailDetails.vue";

export default defineComponent({
  name: "ViewDataRequest",
  components: { EmailDetails, PrimeDialog, PrimeButton, BackButton, AuthenticationWrapper, TheHeader, TheFooter },
  props: {
    requestId: {
      type: String,
      required: true,
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  data() {
    return {
      storedDataRequest: {} as StoredDataRequest,
      companyName: "",
      showNewMessageDialog: false,
      emailContacts: new Set<string>(),
      emailMessage: "",
      hasValidEmailForm: false,
    };
  },
  mounted() {
    this.getRequest()
      .catch((error) => console.error(error))
      .then(() => {
        this.getCompanyName(this.storedDataRequest.datalandCompanyId).catch((error) => console.error(error));
      })
      .catch((error) => console.error(error));
  },
  methods: {
    convertUnixTimeInMsToDateString,
    badgeClass,
    getFrameworkSubtitle,
    frameworkHasSubTitle,
    getFrameworkTitle,
    /**
     * Method to update the email fields
     * @param hasValidForm boolean indicating if the input is correct
     * @param contacts email addresses
     * @param message the content
     */
    updateEmailFields(hasValidForm: boolean, contacts: Set<string>, message: string) {
      this.hasValidEmailForm = hasValidForm;
      this.emailContacts = contacts;
      this.emailMessage = message;
    },
    /**
     * Method to get the request from the api
     */
    async getRequest() {
      try {
        if (this.getKeycloakPromise) {
          this.storedDataRequest = (
            await new ApiClientProvider(this.getKeycloakPromise()).apiClients.requestController.getDataRequestById(
              this.requestId,
            )
          ).data;
        }
      } catch (error) {
        console.error(error);
      }
    },
    /**
     * Method to get the company Name from the backend
     * @param companyId companyId
     */
    async getCompanyName(companyId: string) {
      try {
        if (this.getKeycloakPromise) {
          this.companyName = (
            await new ApiClientProvider(this.getKeycloakPromise()).backendClients.companyDataController.getCompanyInfo(
              companyId,
            )
          ).data.companyName;
        }
      } catch (error) {
        console.error(error);
      }
    },
    /**
     * Method to withdraw the request when clicking on the button
     */
    withdrawRequest() {
      patchDataRequestStatus(
        this.requestId,
        RequestStatus.Withdrawn as RequestStatus,
        undefined,
        undefined,
        this.getKeycloakPromise,
      )
        .catch((error) => console.error(error))
        .then(() => window.location.reload())
        .catch((error) => console.error(error));
    },
    /**
     * Method to update the request message when clicking on the button
     */
    addMessage() {
      if (this.hasValidEmailForm) {
        patchDataRequestStatus(
          this.requestId,
          undefined,
          this.emailContacts,
          this.emailMessage,
          this.getKeycloakPromise,
        )
          .catch((error) => console.error(error))
          .then(() => window.location.reload())
          .catch((error) => console.error(error));
      }
    },
    /**
     * Method to check if request is withdrawAble
     * @returns boolean is withdrawAble
     */
    isWithdrawAble() {
      return (
        this.storedDataRequest.requestStatus == RequestStatus.Open ||
        this.storedDataRequest.requestStatus == RequestStatus.Answered
      );
    },
    /**
     * Navigates to the company view page
     * @returns the promise of the router push action
     */
    goToResolveDataRequestPage() {
      const url = `/companies/${this.storedDataRequest.datalandCompanyId}/frameworks/${this.storedDataRequest.dataType}`;
      return this.$router.push(url);
    },
    /**
     * Method to check if request status is answered
     * @returns boolean if request status is answered
     */
    isRequestStatusAnswered() {
      return this.storedDataRequest.requestStatus == RequestStatus.Answered;
    },
    /**
     * Method to check if request status is answered or closed
     * @returns boolean if request status is answered or closed
     */
    isDatasetAvailable() {
      //todo check if dataset exists, not just by status
      return (
        this.storedDataRequest.requestStatus == RequestStatus.Answered ||
        this.storedDataRequest.requestStatus == RequestStatus.Closed
      );
    },
    /**
     * Method to transform set of string to one string representing the set elements seperated by ','
     * @param contacts set of strings
     * @returns string representing the elements of the set
     */
    formattedContacts(contacts: Set<string>) {
      const contactsList = [...contacts];
      return contactsList.join(", ");
    },
    /**
     * Shows or hides the Modal depending on the current state
     */
    openMessageDialog() {
      this.showNewMessageDialog = true;
    },
    /**
     * Method to check if request status is open
     * @returns boolean if request status is open
     */
    allowNewMessage() {
      return this.storedDataRequest.requestStatus == RequestStatus.Open;
    },
  },
});
</script>
<style lang="scss" scoped>
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

.card {
  width: 100%;
  background-color: var(--surface-card);
  padding: $spacing-md;
  border-radius: $radius-xxs;
  text-align: left;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  margin-bottom: 1rem;

  &__subtitle {
    font-size: medium;
    line-height: normal;
    color: gray;
  }

  &__data {
    font-size: medium;
    font-weight: bold;
    line-height: normal;
    margin-top: 0.25rem;
    margin-bottom: 2rem;
  }

  &__title {
    font-size: large;
    font-weight: bold;
    line-height: normal;
  }

  &__separator {
    width: 100%;
    border-bottom: #e0dfde solid 1px;
    margin-top: 1rem;
    margin-bottom: 1rem;
  }
}
</style>
