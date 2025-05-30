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

      <PrimeDialog
        id="reopenModal"
        :dismissableMask="true"
        :modal="true"
        v-model:visible="reopenModalIsVisible"
        :closable="true"
        style="text-align: left; height: fit-content; width: 21vw"
        data-test="reopenModal"
        class="modal pl-2"
      >
        <template #header>
          <span style="font-weight: bold; margin-right: auto">REOPEN REQUEST</span>
        </template>

        <FormKit type="form" :actions="false" class="formkit-wrapper">
          <label for="Message">
            <b style="margin-bottom: 8px">Message</b>
          </label>
          <FormKit v-model="reopenMessage" type="textarea" name="reopenMessage" data-test="reopenMessage" />
          <p
            v-show="reopenMessageError && reopenMessage.length < 10"
            class="text-danger"
            data-test="noMessageErrorMessage"
          >
            You have not provided a sufficient reason yet. Please provide a reason.
          </p>
          <p class="gray-text font-italic" style="text-align: left">
            Please enter the reason why you think that the dataset should be available. Your message will be forwarded
            to the data provider.
          </p>
        </FormKit>
        <div>
          <PrimeButton
            data-test="reopenRequestButton"
            @click="reopenRequest()"
            style="width: 100%; justify-content: center"
          >
            <span class="d-letters" style="text-align: center" data-test="reopenButton"> REOPEN REQUEST </span>
          </PrimeButton>
        </div>
      </PrimeDialog>

      <PrimeDialog
        id="reopenedModal"
        :dismissableMask="true"
        :modal="true"
        v-model:visible="reopenedModalIsVisible"
        :closable="false"
        style="border-radius: 0.75rem; text-align: center"
        :show-header="false"
        data-test="reopenedModal"
      >
        <div class="text-center" style="display: flex; flex-direction: column">
          <div style="margin: 10px">
            <em class="material-icons info-icon green-text" style="font-size: 2.5em"> check_circle </em>
          </div>
          <div style="margin: 10px">
            <h2 class="m-0" data-test="successText">Reopened</h2>
          </div>
        </div>
        <div class="text-block" style="margin: 15px; white-space: pre">
          You have successfully reopened your data request.
        </div>
        <div style="margin: 10px">
          <PrimeButton label="CLOSE" @click="reopenedModalIsVisible = false" class="p-button-outlined" />
        </div>
      </PrimeDialog>

      <div class="py-4 paper-section">
        <div class="grid col-9 justify-content-around">
          <div class="col-4">
            <div class="card" data-test="card_requestDetails">
              <div class="card__title">Request Details</div>
              <div class="card__separator" />
              <div class="card__subtitle" v-if="isUserKeycloakAdmin">Requester</div>
              <div class="card__data" v-if="isUserKeycloakAdmin">{{ storedDataRequest.userEmailAddress }}</div>
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
              v-show="answeringDataSetUrl"
              class="link claim-panel-text"
              style="font-weight: bold"
              data-test="viewDataset"
              @click="goToAnsweringDataSetPage()"
            >
              VIEW DATASET
            </div>
          </div>
          <div class="grid col-8 flex-direction-column">
            <div class="col-12">
              <div class="card" data-test="card_requestIs">
                <span style="display: flex; align-items: center">
                  <span class="card__title">Request is:</span>
                  <span :class="badgeClass(storedDataRequest.requestStatus)" style="display: inline-flex">
                    {{ getRequestStatusLabel(storedDataRequest.requestStatus) }}
                  </span>
                  <span class="card__title">and Access is:</span>
                  <span :class="accessStatusBadgeClass(storedDataRequest.accessStatus)" style="display: inline-flex">
                    {{ storedDataRequest.accessStatus }}
                  </span>
                  <span class="card__subtitle">
                    since {{ convertUnixTimeInMsToDateString(storedDataRequest.lastModifiedDate) }}
                  </span>
                  <span style="margin-left: auto">
                    <ReviewRequestButtons
                      v-if="isUsersOwnRequest && isRequestStatusAnswered()"
                      @request-reopened-or-resolved="initializeComponent()"
                      :data-request-id="storedDataRequest.dataRequestId"
                    />
                  </span>
                </span>
                <div class="card__separator" />
                <StatusHistory :status-history="storedDataRequest.dataRequestStatusHistory" />
              </div>
              <div class="card" data-test="notifyMeImmediately" v-if="isUsersOwnRequest">
                <span class="card__title" style="margin-right: auto">Notify Me Immediately</span>
                <div class="card__separator" />
                Receive emails directly or via summary
                <InputSwitch
                  style="margin: 1rem 0"
                  data-test="notifyMeImmediatelyInput"
                  inputId="notifyMeImmediatelyInput"
                  v-model="storedDataRequest.notifyMeImmediately"
                  @update:modelValue="changeReceiveEmails()"
                />
                <label for="notifyMeImmediatelyInput">
                  <strong v-if="storedDataRequest.notifyMeImmediately">immediate update</strong>
                  <span v-else>weekly summary</span>
                </label>
              </div>
              <div class="card" data-test="card_providedContactDetails" v-if="isUsersOwnRequest">
                <span style="display: flex; align-items: center">
                  <span class="card__title" style="margin-right: auto">Provided Contact Details and Messages</span>
                  <span
                    v-show="isNewMessageAllowed()"
                    style="cursor: pointer; display: flex; align-items: center"
                    @click="openMessageDialog()"
                    data-test="newMessage"
                  >
                    <i class="pi pi-file-edit pl-3 pr-3" aria-hidden="true" />
                    <span style="font-weight: bold">NEW MESSAGE</span>
                  </span>
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
              <div class="card" v-show="isRequestReopenable(storedDataRequest.requestStatus)" data-test="card_reopen">
                <div class="card__title">Reopen Request</div>
                <div class="card__separator" />
                <div>
                  Currently, your request has the status non-sourceable. If you believe that your data should be
                  available, you can reopen the request and comment why you believe the data should be available.<br />
                  <br />
                  <a
                    class="link"
                    style="display: inline-flex; font-weight: bold; color: #e67f3f"
                    @click="openModalReopenRequest()"
                  >
                    Reopen request</a
                  >
                </div>
              </div>
              <div class="card" v-show="isRequestWithdrawable()" data-test="card_withdrawn">
                <div class="card__title">Withdraw Request</div>
                <div class="card__separator" />
                <div>
                  Once a data request is withdrawn, it will be removed from your data request list. The company owner
                  will not be notified anymore.<br />
                  <br />
                  <a
                    class="link"
                    style="display: inline-flex; font-weight: bold; color: #e67f3f"
                    @click="withdrawRequest()"
                  >
                    Withdraw request</a
                  >
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <TheFooter />
    </TheContent>
  </AuthenticationWrapper>
</template>

<script lang="ts">
import BackButton from '@/components/general/BackButton.vue';
import TheContent from '@/components/generics/TheContent.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import EmailDetails from '@/components/resources/dataRequest/EmailDetails.vue';
import ReviewRequestButtons from '@/components/resources/dataRequest/ReviewRequestButtons.vue';
import StatusHistory from '@/components/resources/dataRequest/StatusHistory.vue';
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import router from '@/router';
import { ApiClientProvider } from '@/services/ApiClients';
import { getAnsweringDataSetUrl } from '@/utils/AnsweringDataset.ts';
import { getCompanyName } from '@/utils/CompanyInformation.ts';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import { KEYCLOAK_ROLE_ADMIN } from '@/utils/KeycloakRoles';
import { checkIfUserHasRole, getUserId } from '@/utils/KeycloakUtils';
import { accessStatusBadgeClass, badgeClass, getRequestStatusLabel, patchDataRequest } from '@/utils/RequestUtils';
import { frameworkHasSubTitle, getFrameworkSubtitle, getFrameworkTitle } from '@/utils/StringFormatter';
import { RequestStatus, type StoredDataRequest } from '@clients/communitymanager';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import PrimeDialog from 'primevue/dialog';
import InputSwitch from 'primevue/inputswitch';
import { defineComponent, inject } from 'vue';

export default defineComponent({
  name: 'ViewDataRequest',
  components: {
    ReviewRequestButtons,
    TheContent,
    EmailDetails,
    PrimeDialog,
    PrimeButton,
    InputSwitch,
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
      reopenModalIsVisible: false,
      reopenMessage: '',
      reopenedModalIsVisible: false,
      isUsersOwnRequest: false,
      isUserKeycloakAdmin: false,
      storedDataRequest: {} as StoredDataRequest,
      companyName: '',
      showNewMessageDialog: false,
      emailContacts: undefined as string[] | undefined,
      emailMessage: undefined as string | undefined,
      hasValidEmailForm: false,
      reopenMessageError: false,
      answeringDataSetUrl: undefined as string | undefined,
    };
  },
  mounted() {
    this.initializeComponent();
  },
  methods: {
    getRequestStatusLabel,
    accessStatusBadgeClass,
    convertUnixTimeInMsToDateString,
    badgeClass,
    getFrameworkSubtitle,
    frameworkHasSubTitle,
    getFrameworkTitle,
    /**
     * Perform all steps required to set up the component.
     */
    initializeComponent() {
      this.getRequest()
        .catch((error) => console.error(error))
        .then(() => {
          if (this.getKeycloakPromise) {
            const apiClientProvider = new ApiClientProvider(this.getKeycloakPromise());
            this.getAndStoreCompanyName(this.storedDataRequest.datalandCompanyId, apiClientProvider).catch((error) =>
              console.error(error)
            );
            this.checkForAvailableData(this.storedDataRequest, apiClientProvider).catch((error) =>
              console.error(error)
            );
          }
          this.storedDataRequest.dataRequestStatusHistory.sort((a, b) => b.creationTimestamp - a.creationTimestamp);
          void this.setUserAccessFields();
        })
        .catch((error) => console.error(error));
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
    /**
     * Retrieve the company name and store it if a value was found.
     */
    async getAndStoreCompanyName(companyId: string, apiClientProvider: ApiClientProvider) {
      try {
        this.companyName = await getCompanyName(companyId, apiClientProvider);
      } catch (error) {
        console.error(error);
      }
    },
    /**
     * Method to check if there exist an approved dataset for a dataRequest
     * @param storedDataRequest dataRequest
     * @param apiClientProvider the ApiClientProvider to use for the connection
     */
    async checkForAvailableData(storedDataRequest: StoredDataRequest, apiClientProvider: ApiClientProvider) {
      try {
        this.answeringDataSetUrl = await getAnsweringDataSetUrl(storedDataRequest, apiClientProvider);
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
     * Method to check if a request can be reopened (only for nonSourceable requests)
     * @param requestStatus request status of the dataland request
     * @returns true if request status is non sourceable otherwise false
     */
    isRequestReopenable(requestStatus: RequestStatus) {
      return requestStatus == RequestStatus.NonSourceable;
    },
    /**
     * Opens a pop-up window to get the users message why the nonSourceable request should be reopened
     */
    openModalReopenRequest() {
      this.reopenModalIsVisible = true;
    },
    /**
     * Method to change if the user wants to receive emails on updates
     */
    async changeReceiveEmails() {
      try {
        await patchDataRequest(
          this.requestId,
          undefined,
          undefined,
          undefined,
          undefined,
          this.storedDataRequest.notifyMeImmediately,
          undefined,
          this.getKeycloakPromise
        );
      } catch (error) {
        console.error(error);
        return;
      }
    },
    /**
     * Method to reopen the non sourceable data request
     */
    async reopenRequest() {
      if (this.reopenMessage.length > 10) {
        try {
          await patchDataRequest(
            this.storedDataRequest.dataRequestId,
            RequestStatus.Open as RequestStatus,
            undefined,
            undefined,
            undefined,
            undefined,
            this.reopenMessage,
            this.getKeycloakPromise
          );
          this.reopenModalIsVisible = false;
          this.reopenedModalIsVisible = true;
          this.storedDataRequest.requestStatus = RequestStatus.Open;
          this.reopenMessage = '';
        } catch (error) {
          console.log(error);
        }
        return;
      }
      this.reopenMessageError = true;
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
     * This function sets the components fields 'isUsersOwnRequest' and 'isUserKeycloakAdmin'.
     * Both variables are used to show information on page depending on who's visiting
     */
    async setUserAccessFields() {
      try {
        if (this.getKeycloakPromise) {
          const userId = await getUserId(this.getKeycloakPromise);
          this.isUsersOwnRequest = this.storedDataRequest.userId == userId;
          this.isUserKeycloakAdmin = await checkIfUserHasRole(KEYCLOAK_ROLE_ADMIN, this.getKeycloakPromise);
        }
      } catch (error) {
        console.error(error);
        return;
      }
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
          // as unknown as Set<string> cast required to ensure proper json is created
          this.emailContacts as unknown as Set<string>,
          this.emailMessage,
          undefined,
          undefined,
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
        this.storedDataRequest.requestStatus == RequestStatus.Answered ||
        this.storedDataRequest.requestStatus == RequestStatus.NonSourceable
      );
    },
    /**
     * Navigates to the company view page
     * @returns the promise of the router push action
     */
    goToAnsweringDataSetPage() {
      if (this.answeringDataSetUrl) return router.push(this.answeringDataSetUrl);
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
@use '@/assets/scss/variables';

.message {
  width: 100%;
  border: #e0dfde solid 1px;
  padding: variables.$spacing-md;
  border-radius: variables.$radius-xxs;
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
    padding: variables.$spacing-md;
    border-radius: variables.$radius-xxs;
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
