<template>
  <TheContent>
    <div class="headline" style="margin: var(--spacing-xs) 0 0 var(--spacing-md)">
      <h1 class="text-left">Data Request</h1>
    </div>

    <SuccessDialog
      :visible="withdrawSuccessModalIsVisible"
      message="The request has been successfully withdrawn."
      @close="
        () => {
          withdrawSuccessModalIsVisible = false;
          initializeComponent();
        }
      "
    />
    <SuccessDialog
      :visible="resubmitSuccessModalIsVisible"
      message="Your request has been successfully resubmitted."
      @close="goToNewRequestPage()"
    />

    <PrimeDialog
      :modal="true"
      v-model:visible="resubmitModalIsVisible"
      :closable="true"
      style="text-align: left; height: fit-content; width: 22rem"
      data-test="resubmit-modal"
      header="Resubmit Request"
    >
      <div class="message">
        <p class="side-header">Message</p>
        <Textarea
          v-model="resubmitMessage"
          style="resize: none"
          data-test="resubmit-message"
          rows="5"
          placeholder="Provide a reason for resubmitting."
        />
        <Message
          v-if="resubmitMessageError && resubmitMessage.length < 10"
          severity="error"
          variant="simple"
          size="small"
          data-test="noMessageErrorMessage"
        >
          You have not provided a sufficient reason yet. Please provide a reason.
        </Message>
        <p class="dataland-info-text small" style="text-align: left">
          Please enter the reason why you want to resubmit your request. Your message will be forwarded to the data
          provider.
        </p>
      </div>
      <PrimeButton
        data-test="resubmit-confirmation-button"
        @click="resubmitRequest()"
        label="RESUBMIT REQUEST"
        style="align-self: center"
      />
    </PrimeDialog>
    <div style="display: flex">
      <div style="padding: var(--spacing-md)">
        <div class="card" data-test="card_requestDetails">
          <div class="title">Request Details</div>
          <Divider />
          <div v-if="isUserKeycloakAdmin" class="side-header">Requester</div>
          <div class="data" v-if="isUserKeycloakAdmin">{{ userEmail }}</div>
          <div class="side-header">Company</div>
          <div class="data">{{ companyName }}</div>
          <div class="side-header">Framework</div>
          <div class="data">
            {{ getFrameworkTitle(storedRequest.dataType) }}

            <div
              v-show="frameworkHasSubTitle(storedRequest.dataType)"
              style="color: gray; font-size: smaller; line-height: 0.5; white-space: nowrap"
            >
              <br />
              {{ getFrameworkSubtitle(storedRequest.dataType) }}
            </div>
          </div>
          <div class="side-header">Reporting year</div>
          <div class="data">{{ storedRequest.reportingPeriod }}</div>
          <PrimeButton
            v-if="answeringDataSetUrl"
            data-test="view-dataset-button"
            label="VIEW DATASET"
            @click="goToAnsweringDataSetPage()"
            style="width: fit-content"
          />
        </div>
      </div>
      <div>
        <div style="padding: var(--spacing-md)">
          <div class="card" data-test="card_requestIs">
            <span style="display: flex; align-items: center">
              <span class="title">Request is:</span>
              <DatalandTag
                :severity="storedRequest.state || ''"
                :value="storedRequest.state"
                class="dataland-inline-tag"
              />
              <span class="dataland-info-text normal">
                since {{ convertUnixTimeInMsToDateString(storedRequest.lastModifiedDate) }}
              </span>
            </span>
            <Divider />
            <p class="title">Request State History</p>
            <RequestStateHistory :stateHistory="requestHistory" />
          </div>
          <div class="card" v-show="isRequestResubmittable()" data-test="card-resubmit">
            <div class="title">Resubmit Request</div>
            <Divider />
            <p class="dataland-info-text normal" style="align-items: baseline">
              Currently, your request has the state {{ storedRequest.state }}. If you believe that your data should be
              available, you can resubmit the request and comment why you believe the data should be available.
            </p>
            <PrimeButton
              data-test="resubmit-request-button"
              label="RESUBMIT REQUEST"
              @click="resubmitModalIsVisible = true"
              variant="outlined"
              style="width: fit-content"
            />
          </div>
          <div class="card" v-show="isRequestWithdrawable()" data-test="card_withdrawn">
            <div class="title">Withdraw Request</div>
            <Divider />
            <p class="dataland-info-text normal" style="align-items: baseline">
              If you want to stop the processing of this request, you can withdraw it. The data provider will no longer
              process this request.
            </p>
            <PrimeButton
              data-test="withdraw-request-button"
              label="WITHDRAW REQUEST"
              @click="withdrawRequest()"
              variant="outlined"
              style="width: fit-content"
            />
          </div>
        </div>
      </div>
    </div>
  </TheContent>
</template>

<script setup lang="ts">
import { ref, reactive, inject, onMounted, defineProps } from 'vue';
import DatalandTag from '@/components/general/DatalandTag.vue';
import TheContent from '@/components/generics/TheContent.vue';
import RequestStateHistory from '@/components/resources/dataRequest/RequestStateHistory.vue';
import SuccessDialog from '@/components/general/SuccessDialog.vue';
import router from '@/router';
import { type NavigationFailure } from 'vue-router';
import { ApiClientProvider } from '@/services/ApiClients';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import { KEYCLOAK_ROLE_ADMIN } from '@/utils/KeycloakRoles';
import { checkIfUserHasRole, getUserId } from '@/utils/KeycloakUtils';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import { frameworkHasSubTitle, getFrameworkSubtitle, getFrameworkTitle } from '@/utils/StringFormatter';
import { RequestState, type SingleRequest, type StoredRequest } from '@clients/datasourcingservice';
import { type DataMetaInformation, type DataTypeEnum, IdentifierType } from '@clients/backend';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import PrimeDialog from 'primevue/dialog';
import Textarea from 'primevue/textarea';
import Divider from 'primevue/divider';
import Message from 'primevue/message';

const props = defineProps<{ requestId: string }>();
const requestId = ref<string>(props.requestId);

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const requestControllerApi = apiClientProvider.apiClients.requestController;
const companyControllerApi = apiClientProvider.backendClients.companyDataController;
const metaDataControllerApi = apiClientProvider.backendClients.metaDataController;

const withdrawSuccessModalIsVisible = ref(false);
const resubmitModalIsVisible = ref(false);
const resubmitMessage = ref('');
const resubmitSuccessModalIsVisible = ref(false);
const newRequestId = ref<string>('');
const isUsersOwnRequest = ref(false);
const isUserKeycloakAdmin = ref(false);
const storedRequest = reactive({} as StoredRequest);
const companyName = ref('');
const resubmitMessageError = ref(false);
const answeringDataSetUrl = ref(undefined as string | undefined);
const requestHistory = ref<StoredRequest[]>([]);
const userEmail = ref('');

/**
 * Perform all steps required to set up the component.
 */
async function initializeComponent(): Promise<void> {
  await getRequest()
    .catch((error) => console.error(error))
    .then(async () => {
      if (getKeycloakPromise) {
        await getAndStoreCompanyName().catch((error) => console.error(error));
        await getAndStoreRequestHistory().catch((error) => console.error(error));
        await checkForAvailableData().catch((error) => console.error(error));
      }
      requestHistory.value.sort((a, b) => b.creationTimeStamp - a.creationTimeStamp);
      await setUserAccessFields();
    })
    .catch((error) => console.error(error));
  if (getKeycloakPromise) {
    const keycloak = await getKeycloakPromise();
    userEmail.value = keycloak.tokenParsed?.email || '';
  }
}

/**
 * Retrieve the company name and store it if a value was found.
 */
async function getAndStoreCompanyName(): Promise<void> {
  try {
    companyName.value = (await companyControllerApi.getCompanyInfo(storedRequest.companyId)).data.companyName;
  } catch (error) {
    console.error(error);
  }
}

/**
 * Retrieve the request history and store it if a value was found.
 */
async function getAndStoreRequestHistory(): Promise<void> {
  try {
    requestHistory.value = (await requestControllerApi.getRequestHistoryById(requestId.value)).data;
  } catch (error) {
    console.error(error);
  }
}

/**
 * Method to check if there exist an approved dataset for a dataRequest
 */
async function checkForAvailableData(): Promise<void> {
  try {
    answeringDataSetUrl.value = await getAnsweringDataSetUrl();
  } catch (error) {
    console.error(error);
  }
}

/**
 * Retrieves a URL to the data set that is answering the given request. This function may throw an exception.
 */
async function getAnsweringDataSetUrl(): Promise<string | undefined> {
  let answeringDataMetaInfo = await getDataMetaInfo(storedRequest.companyId);
  if (!answeringDataMetaInfo) {
    const parentCompanyId = await getParentCompanyId();
    if (!parentCompanyId) return;
    answeringDataMetaInfo = await getDataMetaInfo(parentCompanyId);
  }
  if (answeringDataMetaInfo)
    return `/companies/${answeringDataMetaInfo.companyId}/frameworks/${answeringDataMetaInfo.dataType}`;
}

/**
 * Retrieve the metadata object of the active data set identified by the given parameters.
 *
 * This function may throw an exception.
 * @return the metadata object if found, else "undefined"
 */
async function getDataMetaInfo(companyId: string): Promise<DataMetaInformation | undefined> {
  const datasets = await metaDataControllerApi.getListOfDataMetaInfo(
    companyId,
    storedRequest.dataType as DataTypeEnum,
    true,
    storedRequest.reportingPeriod
  );
  return datasets.data.length > 0 ? datasets.data[0] : undefined;
}

/**
 * Get the id of the parent company. This function may throw an exception.
 */
async function getParentCompanyId(): Promise<string | undefined> {
  const companyInformation = (await companyControllerApi.getCompanyInfo(storedRequest.companyId)).data;
  if (!companyInformation?.parentCompanyLei) return undefined;

  return (await companyControllerApi.getCompanyIdByIdentifier(IdentifierType.Lei, companyInformation.parentCompanyLei))
    .data.companyId;
}

/**
 * Method to get the request from the api
 */
async function getRequest(): Promise<void> {
  try {
    if (getKeycloakPromise) {
      const result = await requestControllerApi.getRequest(requestId.value);
      Object.assign(storedRequest, result.data);
    }
  } catch (error) {
    console.error(error);
  }
}

/**
 * Method to check if a request can be resubmitted
 * @returns true if request can be resubmitted otherwise false
 */
function isRequestResubmittable(): boolean {
  return storedRequest.state == RequestState.Processed || storedRequest.state == RequestState.Withdrawn;
}

/**
 * Method to resubmit the data request
 */
async function resubmitRequest(): Promise<void> {
  if (resubmitMessage.value.length > 10) {
    try {
      const request: SingleRequest = {
        companyIdentifier: storedRequest.companyId,
        dataType: storedRequest.dataType,
        reportingPeriod: storedRequest.reportingPeriod,
        memberComment: resubmitMessage.value,
      };
      const response = await requestControllerApi.createRequest(request, storedRequest.userId);
      newRequestId.value = response.data.requestId;
      resubmitModalIsVisible.value = false;
      resubmitSuccessModalIsVisible.value = true;
      resubmitMessage.value = '';
    } catch (error) {
      console.log(error);
    }
    return;
  }
  resubmitMessageError.value = true;
}

/**
 * Method to withdraw the request when clicking on the button
 */
async function withdrawRequest(): Promise<void> {
  try {
    await requestControllerApi.patchRequestState(requestId.value, RequestState.Withdrawn);
  } catch (error) {
    console.error(error);
    return;
  }
  withdrawSuccessModalIsVisible.value = true;
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
    (storedRequest.state == RequestState.Open ||
      storedRequest.state == RequestState.Processing ||
      storedRequest.state == RequestState.Processed) &&
    isUserKeycloakAdmin.value
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
 * Navigates to the new request page after resubmission
 */
function goToNewRequestPage(): void {
  resubmitSuccessModalIsVisible.value = false;
  router.push(`/requests/${newRequestId.value}`).catch(console.error);
  requestId.value = newRequestId.value;
  newRequestId.value = '';
  void initializeComponent();
}

onMounted(() => {
  void initializeComponent();
});
</script>

<style scoped>
.side-header {
  font-weight: var(--font-weight-bold);
  margin-top: var(--spacing-md);
}

.message {
  width: 100%;
  display: flex;
  flex-direction: column;
}

.card {
  padding: var(--spacing-lg);
  text-align: left;
  display: flex;
  flex-direction: column;
}

.data {
  color: gray;
  margin-bottom: var(--spacing-xl);
}

.title {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  line-height: normal;
}

.dataland-inline-tag {
  margin: 0 var(--spacing-xs);
}
</style>
