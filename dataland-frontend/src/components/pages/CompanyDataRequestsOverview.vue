<template>
  <TheContent class="min-h-screen relative">
    <div v-if="waitingForData || storedDataRequests.length > 0">
      <div class="search-bar-and-filters-container">
        <IconField class="request-company-search-bar-container">
          <InputIcon class="pi pi-search" />
          <InputText
              data-test="requested-datasets-searchbar"
              v-model="searchBarInput"
              placeholder="Search by requester"
              variant="filled"
              fluid
          />
        </IconField>
        <FrameworkDataSearchDropdownFilter
            v-model="selectedFrameworks"
            :available-items="availableFrameworks"
            filter-name="Framework"
            data-test="requested-datasets-frameworks"
            filter-placeholder="Search frameworks"
        />
        <PrimeButton variant="link" @click="resetFilterAndSearchBar" label="RESET" data-test="reset-filter" />
      </div>

      <div class="col-12 text-left p-3">
        <div class="card">
          <DataTable
              :value="displayedData"
              style="cursor: pointer"
              :rowHover="true"
              :loading="waitingForData"
              data-test="requested-datasets-table"
              paginator
              paginator-position="bottom"
              :rows="datasetsPerPage"
              :total-records="numberOfFilteredRequests"
              id="my-company-requests-overview-table"
          >
            <Column header="REQUESTER" field="userEmailAddress" :sortable="true">
              <template #body="slotProps">
                {{ slotProps.data.userEmailAddress }}
              </template>
            </Column>
            <Column header="FRAMEWORK" :sortable="true" field="dataType">
              <template #body="slotProps">
                <div>
                  {{ getFrameworkTitle(slotProps.data.dataType) }}
                </div>
                <div
                    data-test="framework-subtitle"
                    v-if="frameworkHasSubTitle(slotProps.data.dataType)"
                    style="color: gray; font-size: smaller; line-height: 0.5; white-space: nowrap"
                >
                  <br />
                  {{ getFrameworkSubtitle(slotProps.data.dataType) }}
                </div>
              </template>
            </Column>
            <Column header="REPORTING PERIOD" field="reportingPeriod" :sortable="true">
              <template #body="slotProps">
                {{ slotProps.data.reportingPeriod }}
              </template>
            </Column>
            <Column header="REQUESTED" field="creationTimestamp" :sortable="true">
              <template #body="slotProps">
                <div>
                  {{ convertUnixTimeInMsToDateString(slotProps.data.creationTimestamp) }}
                </div>
              </template>
            </Column>
            <Column header="LAST UPDATED" :sortable="true" field="lastModifiedDate">
              <template #body="slotProps">
                <div>
                  {{ convertUnixTimeInMsToDateString(slotProps.data.lastModifiedDate) }}
                </div>
              </template>
            </Column>
            <Column header="REQUEST STATUS" :sortable="true" field="requestStatus">
              <template #body="slotProps">
                <div :class="badgeClass(slotProps.data.requestStatus)" style="display: inline-flex">
                  {{ getRequestStatusLabel(slotProps.data.requestStatus) }}
                </div>
              </template>
            </Column>
            <Column header="ACCESS STATUS" :sortable="true" field="accessStatus">
              <template #body="slotProps">
                <div :class="accessStatusBadgeClass(slotProps.data.accessStatus)" style="display: inline-flex">
                  {{ slotProps.data.accessStatus }}
                </div>
              </template>
            </Column>
          </DataTable>
        </div>
      </div>
    </div>
    <div v-if="!waitingForData && storedDataRequests.length == 0">
      <div class="d-center-div text-center px-7 py-4">
        <p class="font-medium text-xl">You have no data requests yet.</p>
      </div>
    </div>
  </TheContent>
</template>

<script setup lang="ts">
import TheContent from '@/components/generics/TheContent.vue';
import FrameworkDataSearchDropdownFilter from '@/components/resources/frameworkDataSearch/FrameworkDataSearchDropdownFilter.vue';
import { ApiClientProvider } from '@/services/ApiClients';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import { type FrameworkSelectableItem, type SelectableItem } from '@/utils/FrameworkDataSearchDropDownFilterTypes';
import {
  customCompareForRequestState,
  retrieveAvailableFrameworks,
} from '@/utils/RequestsOverviewPageUtils';
import { accessStatusBadgeClass, badgeClass, getRequestStatusLabel } from '@/utils/RequestUtilsLegacy';
import { frameworkHasSubTitle, getFrameworkSubtitle, getFrameworkTitle } from '@/utils/StringFormatter';
import {
  type CompanyRoleAssignmentExtended,
} from '@clients/communitymanager';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';
import InputText from 'primevue/inputtext';
import { inject, onMounted, ref, watch } from 'vue';
import {ExtendedStoredRequest} from "@clients/datasourcingservice";

const datasetsPerPage = 100;
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const companyRoleAssignments = inject<Array<CompanyRoleAssignmentExtended>>('companyRoleAssignments');


const waitingForData = ref(true);
const currentPage = ref(0);
const storedDataRequests = ref<ExtendedStoredRequest[]>([]);
const displayedData = ref<ExtendedStoredRequest[]>([]);
const searchBarInput = ref('');
const searchBarInputFilter = ref('');
const availableFrameworks = ref<Array<FrameworkSelectableItem>>([]);
const selectedFrameworks = ref<Array<FrameworkSelectableItem>>([]);
const numberOfFilteredRequests = ref(0);
const sortField = ref<keyof ExtendedStoredRequest>('state');
const sortOrder = ref(1);

/**
 * Gets list of storedComapnyDataRequests
 */
async function getStoredCompanyRequestDataList() {
  waitingForData.value = true;
  storedDataRequests.value = [];
  if (!companyRoleAssignments || !Array.isArray(companyRoleAssignments)) {
    waitingForData.value = false;
    return;
  }
  const companyIDs = Array.from(new Set(companyRoleAssignments.map((assignment) => assignment.companyId)));
  if (getKeycloakPromise) {
    const apiClientProvider = new ApiClientProvider(getKeycloakPromise());
    const dataRequestsPromises = companyIDs.map(async (companyId) => {
      try {
        const response = await apiClientProvider.apiClients.requestController.searchRequests(companyId);
        return response.data;
      } catch (error) {
        console.error(`Error fetching data for companyId ${companyId}:`, error);
        return [];
      }
    });
    storedDataRequests.value = (await Promise.all(dataRequestsPromises)).flat();
  }
  waitingForData.value = false;
}

/**
 * Filterfunction for frameworks
 * @param framework dataland framework
 * @returns checks if given framework is selected
 */
function filterFramework(framework: string) {
  for (const selectedFramework of selectedFrameworks.value) {
    if (framework == selectedFramework.frameworkDataType) return true;
  }
  return false;
}

/**
 * Filterfunction for searchbar
 * @param requesterMail dataland requesterMail
 * @returns checks if given requesterMail contains searchbar text
 */
function filterSearchInput(requesterMail: string | undefined) {
  const lowerCaseRequesterMail = (requesterMail ?? '').toLowerCase();
  const lowerCaseSearchString = searchBarInputFilter.value.toLowerCase();
  return lowerCaseRequesterMail.includes(lowerCaseSearchString);
}

/**
 * Resets selected frameworks and searchBarInput
 */
function resetFilterAndSearchBar() {
  selectedFrameworks.value = [];
  searchBarInput.value = '';
}

/**
 * Compares two  stored data requests (sort field, request status, last modified, requester mail)
 * @param a StoredDataRequest to sort
 * @param b StoredDataRequest to sort
 * @returns result of the comparison
 */
function customCompareForStoredDataRequests(a: ExtendedStoredRequest, b: ExtendedStoredRequest) {
  const aValue = a[sortField.value] ?? '';
  const bValue = b[sortField.value] ?? '';

  if (sortField.value != ('status' as keyof ExtendedStoredRequest)) {
    if (aValue < bValue) return -1 * sortOrder.value;
    if (aValue > bValue) return sortOrder.value;
  }

  if (a.state != b.state)
    return customCompareForRequestState(a.state, b.state, sortOrder.value);

  if (a.lastModifiedDate < b.lastModifiedDate) return sortOrder.value;
  if (a.lastModifiedDate > b.lastModifiedDate) return -1 * sortOrder.value;

  if ((a.userEmailAddress ?? '') < (b.userEmailAddress ?? '')) return -1 * sortOrder.value;
  else return sortOrder.value;
}

/**
 * Updates the displayedData
 */
function updateCurrentDisplayedData() {
  displayedData.value = storedDataRequests.value.filter((dataRequest) =>
      filterSearchInput(dataRequest.companyName)
  );
  if (selectedFrameworks.value.length > 0) {
    displayedData.value = displayedData.value.filter((dataRequest) => filterFramework(dataRequest.dataType));
  }
  displayedData.value.sort((a, b) => customCompareForStoredDataRequests(a, b));
  numberOfFilteredRequests.value = displayedData.value.length;
  displayedData.value = displayedData.value.slice(
      datasetsPerPage * currentPage.value,
      datasetsPerPage * (1 + currentPage.value)
  );
  globalThis.scrollTo({
    top: 0,
    behavior: 'smooth',
  });
}


onMounted(() => {
  availableFrameworks.value = retrieveAvailableFrameworks();
  getStoredCompanyRequestDataList().catch((error) => console.error(error));
});

watch(selectedFrameworks, () => {
  updateCurrentDisplayedData();
});

watch(waitingForData, () => {
  updateCurrentDisplayedData();
});

watch(searchBarInput, (newSearch: string) => {
  searchBarInputFilter.value = newSearch;
  updateCurrentDisplayedData();
});

watch(() => companyRoleAssignments, () => {
  void getStoredCompanyRequestDataList();
}, { deep: true });
</script>

<style scoped>
#my-data-requests-overview-table tr:hover {
  cursor: pointer;
}

.d-center-div {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: white;
}

.text-primary {
  color: var(--main-color);
}

.search-bar-and-filters-container {
  margin-top: var(--spacing-md);
  width: 100%;
  padding: var(--spacing-lg);
  z-index: 100;
  display: flex;
  gap: var(--spacing-md);

  .request-company-search-bar-container {
    width: 25%;
  }
}
</style>