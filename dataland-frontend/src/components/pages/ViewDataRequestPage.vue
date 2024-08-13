<template>
  <AuthenticationWrapper>
    <TheContent class="min-h-screen flex sheet">
      <TheHeader />
      <div class="headline" style="margin-left: 1rem; margin-top: 0.5rem">
        <BackButton />

        <h1 class="text-left">Data Request</h1>
      </div>

      <PrimeDialog
        id="successModal"
        :dismissableMask="true"
        :modal="true"
        v-model:visible="successModalIsVisible"
        :closable="false"
        style="border-radius: 0.75rem; text-align: center"
        :show-header="false"
        data-test="successModal"
      >
        <div class="text-center" style="display: flex; flex-direction: column">
          <div style="margin: 10px">
            <em class="material-icons info-icon green-text" style="font-size: 2.5em"> check_circle </em>
          </div>
          <div style="margin: 10px">
            <h2 class="m-0" data-test="successText">Success</h2>
          </div>
        </div>
        <div class="text-block" style="margin: 15px; white-space: pre">
          You have successfully withdrawn your request.
        </div>
        <div style="margin: 10px">
          <PrimeButton label="CLOSE" @click="successModalIsVisible = false" class="p-button-outlined" />
        </div>
      </PrimeDialog>
      <PrimeDialog
        :dismissableMask="true"
        :modal="true"
        v-if="showNewMessageDialog"
        v-model:visible="showNewMessageDialog"
        :closable="true"
        style="text-align: center"
        :show-header="true"
      >
        <template #header>
          <span style="font-weight: bold; margin-right: auto">NEW MESSAGE</span>
        </template>
        <EmailDetails
          :is-optional="false"
          :show-errors="toggleEmailDetailsError"
          @has-new-input="updateEmailFields"
          data-test="newMessageModal"
        />
        <PrimeButton data-test="addMessageButton" @click="addMessage()" style="width: 100%; justify-content: center">
          <span class="d-letters pl-2" style="text-align: center"> SEND MESSAGE </span>
        </PrimeButton>
      </PrimeDialog>

      <div class="py-4 paper-section">
        <div class="grid col-9 justify-content-around">
          <div class="col-4">
            <div class="card" data-test="card_requestDetails">
              <div class="card__title">Request Details</div>
              <div class="card__separator" />
              <div class="card__subtitle">Company</div>
              <div class="card__data">{{ companyName }}</div>
              <div class="card__subtitle">Framework</div>
              <div class="card__data">
                {{ getFrameworkTitle(storedDataRequest.dataType) }}

                <div
                  v-show="frameworkHasSubTitle(storedDataRequest.dataType)"
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
              v-show="isDatasetAvailable"
              class="link claim-panel-text"
              style="font-weight: bold"
              data-test="viewDataset"
              @click="goToResolveDataRequestPage()"
            >
              VIEW DATASET
            </div>
          </div>
          <div class="grid col-8 flex-direction-column">
            <div class="col-12">
              <div class="card" data-test="card_requestIs">
                <span style="display: flex; align-items: center">
                  <div class="card__title">Request is:</div>
                  <div :class="badgeClass(storedDataRequest.requestStatus)" style="display: inline-flex">
                    {{ storedDataRequest.requestStatus }}
                  </div>
                  <div class="card__title">and Access is:</div>
                  <div :class="accessStatusBadgeClass(storedDataRequest.accessStatus)" style="display: inline-flex">
                    {{ storedDataRequest.accessStatus }}
                  </div>
                  <div class="card__subtitle">
                    since {{ convertUnixTimeInMsToDateString(storedDataRequest.lastModifiedDate) }}
                  </div>
                  <div style="margin-left: auto">
                    <PrimeButton
                      data-test="resolveRequestButton"
                      v-show="isRequestStatusAnswered()"
                      @click="goToResolveDataRequestPage()"
                    >
                      <span class="d-letters pl-2"> Resolve Request </span>
                    </PrimeButton>
                  </div>
                </span>
                <div class="card__separator" />
                <StatusHistory :status-history="storedDataRequest.dataRequestStatusHistory" />
              </div>
              <div class="card" data-test="card_providedContactDetails">
                <span style="display: flex; align-items: center">
                  <div class="card__title" style="margin-right: auto">Provided Contact Details & Messages</div>
                  <div
                    v-show="isNewMessageAllowed()"
                    style="cursor: pointer; display: flex; align-items: center"
                    @click="openMessageDialog()"
                    data-test="newMessage"
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
                    <div style="color: black">Sent to: {{ formatContactsToString(message.contacts) }}</div>
                    <div class="card__separator" />
                    <div style="color: gray">
                      {{ message.message }}
                    </div>
                  </div>
                </div>
              </div>
              <div class="card" v-show="isRequestWithdrawable()" data-test="card_withdrawn">
                <div class="card__title">Withdraw Request</div>
                <div class="card__separator" />
                <div>
                  Once a data request is withdrawn, it will be removed from your data request list. The company owner
                  will not be notified anymore.
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
    </TheContent>
  </AuthenticationWrapper>
</template>

<script lang="ts">
import { defineComponent, inject } from 'vue';
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import BackButton from '@/components/general/BackButton.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import { ApiClientProvider } from '@/services/ApiClients';
import { RequestStatus, type StoredDataRequest } from '@clients/communitymanager';
import type Keycloak from 'keycloak-js';
import { frameworkHasSubTitle, getFrameworkSubtitle, getFrameworkTitle } from '@/utils/StringFormatter';
import { accessStatusBadgeClass, badgeClass, patchDataRequest } from '@/utils/RequestUtils';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import PrimeButton from 'primevue/button';
import PrimeDialog from 'primevue/dialog';
import EmailDetails from '@/components/resources/dataRequest/EmailDetails.vue';
import { type DataTypeEnum, QaStatus } from '@clients/backend';
import TheContent from '@/components/generics/TheContent.vue';
import StatusHistory from '@/components/resources/dataRequest/StatusHistory.vue';

export default defineComponent({
  name: 'ViewDataRequest',
  components: {
    TheContent,
    EmailDetails,
    PrimeDialog,
    PrimeButton,
    BackButton,
    AuthenticationWrapper,
    TheHeader,
    TheFooter,
    StatusHistory,
  },
  props: {
    requestId: {
      type: String,
      required: true,
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  data() {
    return {
      toggleEmailDetailsError: false,
      successModalIsVisible: false,
      isDatasetAvailable: false,
      storedDataRequest: {} as StoredDataRequest,
      companyName: '',
      showNewMessageDialog: false,
      emailContacts: undefined as Set<string> | undefined,
      emailMessage: undefined as string | undefined,
      hasValidEmailForm: false,
    };
  },
  mounted() {
    this.getRequest()
      .catch((error) => console.error(error))
      .then(() => {
        this.getCompanyName(this.storedDataRequest.datalandCompanyId).catch((error) => console.error(error));
        this.checkForAvailableData(this.storedDataRequest).catch((error) => console.error(error));
        this.storedDataRequest.dataRequestStatusHistory.sort((a, b) => b.creationTimestamp - a.creationTimestamp);
      })
      .catch((error) => console.error(error));
  },
  methods: {
    //TODO do we need a withdrawn status in the accessStatus Enum. Currently if a vsme data request is withdrawn
    //TODO it is shown as requestStatus = withdrawn + accessStatus = pending in the frontend
    accessStatusBadgeClass,
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
     * Method to check if there exist an approved dataset for a dataRequest
     * @param storedDataRequest dataRequest
     */
    async checkForAvailableData(storedDataRequest: StoredDataRequest) {
      try {
        if (this.getKeycloakPromise) {
          const dataset = await new ApiClientProvider(
            this.getKeycloakPromise()
          ).backendClients.metaDataController.getListOfDataMetaInfo(
            storedDataRequest.datalandCompanyId,
            storedDataRequest.dataType as DataTypeEnum,
            undefined,
            storedDataRequest.reportingPeriod
          );
          for (const dataMetaInfo of dataset.data) {
            if (dataMetaInfo.qaStatus == QaStatus.Accepted) {
              this.isDatasetAvailable = true;
              return;
            }
          }
        }
      } catch (error) {
        console.error(error);
      }
    },
    /**
     * Method to get the request from the api
     */
    async getRequest() {
      try {
        if (this.getKeycloakPromise) {
          this.storedDataRequest = (
            await new ApiClientProvider(this.getKeycloakPromise()).apiClients.requestController.getDataRequestById(
              this.requestId
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
              companyId
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
    async withdrawRequest() {
      try {
        await patchDataRequest(
          this.requestId,
          RequestStatus.Withdrawn as RequestStatus,
          undefined,
          undefined,
          undefined,
          this.getKeycloakPromise
        );
      } catch (error) {
        console.error(error);
        return;
      }
      this.successModalIsVisible = true;
      this.storedDataRequest.requestStatus = RequestStatus.Withdrawn;
    },
    /**
     * Method to update the request message when clicking on the button
     */
    addMessage() {
      if (this.hasValidEmailForm) {
        patchDataRequest(
          this.requestId,
          undefined,
          undefined,
          this.emailContacts,
          this.emailMessage,
          this.getKeycloakPromise
        )
          .then(() => {
            this.getRequest().catch((error) => console.error(error));
            this.showNewMessageDialog = false;
          })
          .catch((error) => console.error(error));
      } else {
        this.toggleEmailDetailsError = !this.toggleEmailDetailsError;
      }
    },
    /**
     * Method to check if request is withdrawAble
     * @returns boolean is withdrawAble
     */
    isRequestWithdrawable() {
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
     * Method to transform set of string to one string representing the set elements seperated by ','
     * @param contacts set of strings
     * @returns string representing the elements of the set
     */
    formatContactsToString(contacts: Set<string>) {
      const contactsList = Array.from(contacts);
      return contactsList.join(', ');
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
    isNewMessageAllowed() {
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

:deep {
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
}
.two-columns {
  columns: 2;
  -webkit-columns: 2;
  -moz-columns: 2;
  list-style-type: none;
}
</style>
