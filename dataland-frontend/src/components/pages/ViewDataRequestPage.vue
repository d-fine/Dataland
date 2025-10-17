<template>
  <TheContent class="min-h-screen flex sheet">
    <div class="headline" style="margin-left: 1rem; margin-top: 0.5rem">
      <h1 class="text-left">Data Request</h1>
    </div>

    <SuccessDialog
      :visible="withdrawSuccessModalIsVisible"
      message="Your request has been successfully withdrawn."
      @close="withdrawSuccessModalIsVisible = false"
    />
    <SuccessDialog
        :visible="resubmitSuccessModalIsVisible"
        message="Your request has been successfully resubmitted."
        @close="() => {
          resubmitSuccessModalIsVisible = false;
          void router.push(`/requests/${newRequestId}`);
        }"
    />

    <PrimeDialog
      id="resubmitModal"
      :dismissableMask="true"
      :modal="true"
      v-model:visible="resubmitModalIsVisible"
      :closable="true"
      style="text-align: left; height: fit-content; width: 21vw"
      data-test="resubmit-modal"
      class="modal pl-2"
    >
      <template #header>
        <span style="font-weight: bold; margin-right: auto">resubmit REQUEST</span>
      </template>

      <FormKit type="form" :actions="false" class="formkit-wrapper">
        <label for="Message">
          <b style="margin-bottom: 8px">Message</b>
        </label>
        <FormKit v-model="resubmitMessage" type="textarea" name="resubmitMessage" data-test="resubmit-message" />
        <p
          v-show="resubmitMessageError && resubmitMessage.length < 10"
          class="text-danger"
          data-test="noMessageErrorMessage"
        >
          You have not provided a sufficient reason yet. Please provide a reason.
        </p>
        <p class="gray-text font-italic" style="text-align: left">
          Please enter the reason why you want to resubmit your request. Your message will be forwarded to
          the data provider.
        </p>
      </FormKit>
      <PrimeButton data-test="resubmit-confirmation-button" @click="resubmitRequest()" label="RESUBMIT REQUEST" />
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
            <PrimeButton
                v-if="answeringDataSetUrl"
                data-test="viewDatasetButton"
                label="VIEW DATASET"
                @click="goToAnsweringDataSetPage()"
                style="width: auto"
            />
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
              </span>
              <div class="card__separator" />
              <RequestStateHistory :stateHistory="requestHistory" />
            </div>
            <div class="card" v-show="isRequestResubmittable()" data-test="card-resubmit">
              <div class="card__title">Resubmit Request</div>
              <div class="card__separator" />
              <div>
                Currently, your request has the state {{ storedRequest.state }}. If you believe that your data should be
                available, you can resubmit the request and comment why you believe the data should be available.<br />
                <PrimeButton
                    data-test="resubmit-request-button"
                    label="RESUBMIT REQUEST"
                    @click="resubmitModalIsVisible = true"
                    variant="link"
                />
              </div>
            </div>
            <div class="card" v-show="isRequestWithdrawable()" data-test="card_withdrawn">
              <div class="card__title">Withdraw Request</div>
              <div class="card__separator" />
              <div>
                Some placeholder text.
                <PrimeButton
                  data-test="withdraw-request-button"
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
import RequestStateHistory from '@/components/resources/dataRequest/RequestStateHistory.vue';
import SuccessDialog from '@/components/general/SuccessDialog.vue';
import router from '@/router';
import { type NavigationFailure } from 'vue-router';
import { ApiClientProvider } from '@/services/ApiClients';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import { KEYCLOAK_ROLE_ADMIN } from '@/utils/KeycloakRoles';
import {checkIfUserHasRole, getUserId} from '@/utils/KeycloakUtils';
import { frameworkHasSubTitle, getFrameworkSubtitle, getFrameworkTitle } from '@/utils/StringFormatter';
import { RequestState, type SingleRequest, type StoredRequest } from '@clients/datasourcingservice';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import PrimeDialog from 'primevue/dialog';
import {assertDefined} from "@/utils/TypeScriptUtils.ts";
import {type DataMetaInformation, type DataTypeEnum, IdentifierType} from '@clients/backend';

const props = defineProps<{ requestId: string, userEmailAddress: string }>();
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
    requestHistory.value = (await requestControllerApi.getRequestHistoryById(props.requestId)).data;
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

  return (
      await companyControllerApi.getCompanyIdByIdentifier(
          IdentifierType.Lei,
          companyInformation.parentCompanyLei
      )
  ).data.companyId;
}

/**
 * Method to get the request from the api
 */
async function getRequest(): Promise<void> {
  try {
    if (getKeycloakPromise) {
      const result = await requestControllerApi.getRequest(
        props.requestId
      );
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
    await requestControllerApi.patchRequestState(props.requestId, RequestState.Withdrawn);
  } catch (error) {
    console.error(error);
    return;
  }
  withdrawSuccessModalIsVisible.value = true;
  storedRequest.state = RequestState.Withdrawn;
  void initializeComponent();
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
  ) && isUserKeycloakAdmin.value;
}

/**
 * Navigates to the company view page
 * @returns the promise of the router push action
 */
function goToAnsweringDataSetPage(): Promise<void | NavigationFailure | undefined> | void {
  if (answeringDataSetUrl.value) return router.push(answeringDataSetUrl.value);
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
