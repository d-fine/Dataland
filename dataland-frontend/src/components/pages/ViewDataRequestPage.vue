<template>
  <TheContent class="min-h-screen flex sheet">
    <div class="headline" style="margin-left: 1rem; margin-top: 0.5rem">
      <h1 class="text-left">Data Request</h1>
    </div>

    <SuccessDialog
      :visible="successModalIsVisible"
      message="You have successfully withdrawn your request."
      @close="successModalIsVisible = false"
    />

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
          Please enter the reason why you think that the dataset should be available. Your message will be forwarded to
          the data provider.
        </p>
      </FormKit>
      <PrimeButton data-test="reopenRequestButton" @click="reopenRequest()" label="REOPEN REQUEST" />
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
      <PrimeButton label="CLOSE" @click="reopenedModalIsVisible = false" variant="outlined" />
    </PrimeDialog>

    <div class="py-4">
      <div class="grid col-9 justify-content-around">
        <div class="col-4">
          <div class="card" data-test="card_requestDetails">
            <div class="card__title">Request Details</div>
            <div class="card__separator" />
            <div class="card__subtitle" v-if="isUserKeycloakAdmin">Requester</div>
            <div class="card__data" v-if="isUserKeycloakAdmin">{{ props.userEmailAddress }}</div>
            <div class="card__subtitle">Company</div>
            <div class="card__data">{{ companyName }}</div>
            <div class="card__subtitle">Framework</div>
            <div class="card__data">
              {{ getFrameworkTitle(storedRequest.dataType) }}

              <div
                v-show="frameworkHasSubTitle(storedRequest.dataType)"
                style="color: gray; font-size: smaller; line-height: 0.5; white-space: nowrap"
              >
                <br />
                {{ getFrameworkSubtitle(storedRequest.dataType) }}
              </div>
            </div>
            <div class="card__subtitle">Reporting year</div>
            <div class="card__data">{{ storedRequest.reportingPeriod }}</div>
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
                <DatalandTag
                  :severity="storedRequest.state || ''"
                  :value="storedRequest.state"
                  class="dataland-inline-tag"
                />
                <span class="card__subtitle">
                  since {{ convertUnixTimeInMsToDateString(storedRequest.lastModifiedDate) }}
                </span>
                <span style="margin-left: auto">
                  <ReviewRequestButtons
                    v-if="isUsersOwnRequest && isStateProcessed()"
                    @request-reopened-or-resolved="initializeComponent()"
                    :data-request-id="storedRequest.id"
                  />
                </span>
              </span>
              <div class="card__separator" />
              <StatusHistory :status-history="requestHistory" />
            </div>
            <div class="card" v-show="isRequestReopenable(storedRequest.state)" data-test="card_reopen">
              <div class="card__title">Reopen Request</div>
              <div class="card__separator" />
              <div>
                Currently, your request has the status non-sourceable. If you believe that your data should be
                available, you can reopen the request and comment why you believe the data should be available.<br />
                <br />
                <a
                  class="link"
                  style="display: inline-flex; font-weight: bold; color: var(--p-primary-color)"
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
                Once a data request is withdrawn, it will be removed from your data request list. The company owner will
                not be notified anymore. <br />
                <br />
                <PrimeButton
                  data-test="withdrawRequestButton"
                  label="WITHDRAW REQUEST"
                  @click="withdrawRequest()"
                  variant="link"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </TheContent>
</template>

<script setup lang="ts">
import { ref, reactive, inject, onMounted } from 'vue';
import { defineProps } from 'vue';
import DatalandTag from '@/components/general/DatalandTag.vue';
import TheContent from '@/components/generics/TheContent.vue';
import ReviewRequestButtons from '@/components/resources/dataRequest/ReviewRequestButtons.vue';
import StatusHistory from '@/components/resources/dataRequest/StatusHistory.vue';
import router from '@/router';
import { type NavigationFailure } from 'vue-router';
import { ApiClientProvider } from '@/services/ApiClients';
import { getAnsweringDataSetUrl } from '@/utils/AnsweringDataset.ts';
import { getCompanyName } from '@/utils/CompanyInformation.ts';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import { KEYCLOAK_ROLE_ADMIN } from '@/utils/KeycloakRoles';
import {checkIfUserHasRole, getUserId} from '@/utils/KeycloakUtils';
import { patchRequestState } from '@/utils/RequestUtils';
import { frameworkHasSubTitle, getFrameworkSubtitle, getFrameworkTitle } from '@/utils/StringFormatter';
import { RequestState, type StoredRequest } from '@clients/datasourcingservice';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import PrimeDialog from 'primevue/dialog';
import SuccessDialog from '@/components/general/SuccessDialog.vue';

const props = defineProps<{ requestId: string, userEmailAddress: string }>();
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const successModalIsVisible = ref(false);
const reopenModalIsVisible = ref(false);
const reopenMessage = ref('');
const reopenedModalIsVisible = ref(false);
const isUsersOwnRequest = ref(false);
const isUserKeycloakAdmin = ref(false);
const storedRequest = reactive({} as StoredRequest);
const companyName = ref('');
const reopenMessageError = ref(false);
const answeringDataSetUrl = ref(undefined as string | undefined);
const requestHistory = ref<StoredRequest[]>([]);

/**
 * Perform all steps required to set up the component.
 */
async function initializeComponent(): Promise<void> {
  await getRequest()
    .catch((error) => console.error(error))
    .then(async () => {
      if (getKeycloakPromise) {
        const apiClientProvider = new ApiClientProvider(getKeycloakPromise());
        await getAndStoreCompanyName(storedRequest.companyId, apiClientProvider).catch((error) => console.error(error));
        await getAndStoreRequestHistory(props.requestId, apiClientProvider).catch((error) => console.error(error));
        await checkForAvailableData(storedRequest, apiClientProvider).catch((error) => console.error(error));
      }
      requestHistory.value.sort((a, b) => b.creationTimeStamp - a.creationTimeStamp);
      await setUserAccessFields();
    })
    .catch((error) => console.error(error));
}

/**
 * Retrieve the company name and store it if a value was found.
 */
async function getAndStoreCompanyName(companyId: string, apiClientProvider: ApiClientProvider): Promise<void> {
  try {
    companyName.value = await getCompanyName(companyId, apiClientProvider);
  } catch (error) {
    console.error(error);
  }
}

/**
 * Retrieve the request history and store it if a value was found.
 */
async function getAndStoreRequestHistory(requestId: string, apiClientProvider: ApiClientProvider): Promise<void> {
  try {
    requestHistory.value = (await apiClientProvider.apiClients.requestController.getRequestHistoryById(requestId)).data;
  } catch (error) {
    console.error(error);
  }
}

/**
 * Method to check if there exist an approved dataset for a dataRequest
 * @param storedRequest dataRequest
 * @param apiClientProvider the ApiClientProvider to use for the connection
 */
async function checkForAvailableData(
  storedRequest: StoredRequest,
  apiClientProvider: ApiClientProvider
): Promise<void> {
  try {
    answeringDataSetUrl.value = await getAnsweringDataSetUrl(storedRequest, apiClientProvider);
  } catch (error) {
    console.error(error);
  }
}

/**
 * Method to get the request from the api
 */
async function getRequest(): Promise<void> {
  try {
    if (getKeycloakPromise) {
      const result = await new ApiClientProvider(getKeycloakPromise()).apiClients.requestController.getRequest(
        props.requestId
      );
      Object.assign(storedRequest, result.data);
    }
  } catch (error) {
    console.error(error);
  }
}

/**
 * Method to check if a request can be reopened (only for nonSourceable requests)
 * @param state request status of the dataland request
 * @returns true if request status is non sourceable otherwise false
 */
function isRequestReopenable(state: RequestState): boolean {
  return state == RequestState.Processed || state == RequestState.Withdrawn;
}

/**
 * Opens a pop-up window to get the users message why the nonSourceable request should be reopened
 */
function openModalReopenRequest(): void {
  reopenModalIsVisible.value = true;
}

/**
 * Method to reopen the non sourceable data request
 */
async function reopenRequest(): Promise<void> {
  if (reopenMessage.value.length > 10) {
    try {
      await patchRequestState(
        storedRequest.id,
        RequestState.Open,
        getKeycloakPromise
      );
      reopenModalIsVisible.value = false;
      reopenedModalIsVisible.value = true;
      storedRequest.state = RequestState.Open;
      reopenMessage.value = '';
    } catch (error) {
      console.log(error);
    }
    return;
  }
  reopenMessageError.value = true;
}

/**
 * Method to withdraw the request when clicking on the button
 */
async function withdrawRequest(): Promise<void> {
  try {
    await patchRequestState(
      props.requestId,
      RequestState.Withdrawn,
      getKeycloakPromise
    );
  } catch (error) {
    console.error(error);
    return;
  }
  successModalIsVisible.value = true;
  storedRequest.state = RequestState.Withdrawn;
}

/**
 * This function sets the components fields 'isUsersOwnRequest' and 'isUserKeycloakAdmin'.
 * Both variables are used to show information on page depending on who's visiting
 */
async function setUserAccessFields(): Promise<void> {
  try {
    if (getKeycloakPromise) {
      const userId = await getUserId(getKeycloakPromise);
      isUsersOwnRequest.value = storedRequest.userId == userId;
      isUserKeycloakAdmin.value = await checkIfUserHasRole(KEYCLOAK_ROLE_ADMIN, getKeycloakPromise);
    }
  } catch (error) {
    console.error(error);
    return;
  }
}

/**
 * Method to check if request is withdrawAble
 * @returns boolean is withdrawAble
 */
function isRequestWithdrawable(): boolean {
  return (
    storedRequest.state == RequestState.Open ||
    storedRequest.state == RequestState.Processing ||
    storedRequest.state == RequestState.Processed
  );
}

/**
 * Navigates to the company view page
 * @returns the promise of the router push action
 */
function goToAnsweringDataSetPage(): Promise<void | NavigationFailure | undefined> | void {
  if (answeringDataSetUrl.value) return router.push(answeringDataSetUrl.value);
}

/**
 * Method to check if request status is processed
 * @returns boolean if request status is processed
 */
function isStateProcessed(): boolean {
  return storedRequest.state == RequestState.Processed;
}

onMounted(() => {
  void initializeComponent();
});
</script>

<style lang="scss" scoped>
.message {
  width: 100%;
  border: #e0dfde solid 1px;
  padding: var(--spacing-lg);
  text-align: left;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  margin-bottom: 1rem;
  margin-top: 1rem;
}

:deep(*) {
  .card {
    width: 100%;
    background-color: var(--surface-card);
    padding: var(--spacing-lg);
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

.dataland-inline-tag {
  margin: 0 var(--spacing-xs);
}

.info-icon {
  cursor: help;
}

.two-columns {
  columns: 2;
  -webkit-columns: 2;
  -moz-columns: 2;
  list-style-type: none;
}

.flex-direction-column {
  flex-direction: column;
}

.d-letters {
  letter-spacing: 0.05em;
}

.text-danger {
  color: var(--fk-color-error);
  font-size: var(--font-size-xs);
}

.gray-text {
  color: var(--gray);
}

.green-text {
  color: var(--green);
}

.link {
  color: var(--main-color);
  background: transparent;
  border: transparent;
  cursor: pointer;
  display: flex;

  &:hover {
    color: hsl(from var(--main-color) h s calc(l - 20));
    text-decoration: underline;
  }

  &:active {
    color: hsl(from var(--main-color) h s calc(l + 10));
  }

  &:focus {
    box-shadow: 0 0 0 0.2rem var(--btn-focus-border-color);
  }

  &.--underlined {
    text-decoration: underline;
  }
}
</style>
