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
        <FrameworkDataSearchDropdownFilter
          v-model="selectedAccessStatus"
          :available-items="availableAccessStatus"
          filter-name="Access Status"
          data-test="requested-datasets-frameworks"
          filter-placeholder="access status"
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
            <Column field="resolve" header="">
              <template #body="slotProps">
                <div
                  v-if="slotProps.data.accessStatus == AccessStatus.Pending"
                  class="text-right text-primary no-underline font-bold"
                >
                  <div class="button-container">
                    <PrimeButton
                      icon="pi pi-check"
                      @click="updateAccessStatus(slotProps.data.dataRequestId, AccessStatus.Granted)"
                      label="Grant"
                    />
                    <PrimeButton
                      icon="pi pi-times"
                      @click="updateAccessStatus(slotProps.data.dataRequestId, AccessStatus.Declined)"
                      label="Decline"
                    />
                  </div>
                </div>
                <div
                  v-if="slotProps.data.accessStatus == AccessStatus.Granted"
                  class="text-right text-primary no-underline font-bold"
                >
                  <div>
                    <PrimeButton
                      class="button-container"
                      icon="pi pi-ban"
                      @click="updateAccessStatus(slotProps.data.dataRequestId, AccessStatus.Revoked)"
                      label="Revoke"
                    />
                  </div>
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
  <TheFooter />
</template>

<script lang="ts">
import TheContent from '@/components/generics/TheContent.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import FrameworkDataSearchDropdownFilter from '@/components/resources/frameworkDataSearch/FrameworkDataSearchDropdownFilter.vue';
import { ApiClientProvider } from '@/services/ApiClients';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import { type FrameworkSelectableItem, type SelectableItem } from '@/utils/FrameworkDataSearchDropDownFilterTypes';
import {
  customCompareForRequestStatus,
  retrieveAvailableAccessStatus,
  retrieveAvailableFrameworks,
} from '@/utils/RequestsOverviewPageUtils';
import { accessStatusBadgeClass, badgeClass, getRequestStatusLabel } from '@/utils/RequestUtils';
import { frameworkHasSubTitle, getFrameworkSubtitle, getFrameworkTitle } from '@/utils/StringFormatter';
import {
  AccessStatus,
  type CompanyRoleAssignmentExtended,
  type ExtendedStoredDataRequest,
} from '@clients/communitymanager';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';
import InputText from 'primevue/inputtext';
import { defineComponent, inject } from 'vue';

export default defineComponent({
  name: 'MyDataRequestsOverview',
  computed: {
    AccessStatus() {
      return AccessStatus;
    },
  },
  components: {
    PrimeButton,
    FrameworkDataSearchDropdownFilter,
    TheFooter,
    TheContent,
    DataTable,
    Column,
    InputText,
    InputIcon,
    IconField,
  },

  setup() {
    return {
      datasetsPerPage: 100,
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
      companyRoleAssignments: inject<Array<CompanyRoleAssignmentExtended>>('companyRoleAssignments'),
    };
  },

  data() {
    return {
      waitingForData: true,
      currentPage: 0,
      storedDataRequests: [] as ExtendedStoredDataRequest[],
      displayedData: [] as ExtendedStoredDataRequest[],
      searchBarInput: '',
      searchBarInputFilter: '',
      availableFrameworks: [] as Array<FrameworkSelectableItem>,
      selectedFrameworks: [] as Array<FrameworkSelectableItem>,
      availableAccessStatus: [] as Array<SelectableItem>,
      selectedAccessStatus: [] as Array<SelectableItem>,
      numberOfFilteredRequests: 0,
      sortField: 'requestStatus' as keyof ExtendedStoredDataRequest,
      sortOrder: 1,
    };
  },
  mounted() {
    this.availableFrameworks = retrieveAvailableFrameworks();
    this.availableAccessStatus = retrieveAvailableAccessStatus();
    this.getStoredCompanyRequestDataList().catch((error) => console.error(error));
  },
  watch: {
    selectedFrameworks() {
      this.updateCurrentDisplayedData();
    },
    selectedAccessStatus() {
      this.updateCurrentDisplayedData();
    },
    waitingForData() {
      this.updateCurrentDisplayedData();
    },
    searchBarInput(newSearch: string) {
      this.searchBarInputFilter = newSearch;
      this.updateCurrentDisplayedData();
    },
    companyRoleAssignments() {
      void this.getStoredCompanyRequestDataList();
    },
  },
  methods: {
    getRequestStatusLabel,
    accessStatusBadgeClass,
    badgeClass,
    frameworkHasSubTitle,
    getFrameworkTitle,
    getFrameworkSubtitle,
    convertUnixTimeInMsToDateString,

    /**
     * Gets list of storedComapnyDataRequests
     */
    async getStoredCompanyRequestDataList() {
      this.waitingForData = true;
      this.storedDataRequests = [];
      if (!this.companyRoleAssignments) {
        return;
      }
      const companyIDs = Array.from(new Set(this.companyRoleAssignments.map((assignment) => assignment.companyId)));
      if (this.getKeycloakPromise) {
        const apiClientProvider = new ApiClientProvider(this.getKeycloakPromise());
        const dataRequestsPromises = companyIDs.map(async (companyId) => {
          try {
            const response = await apiClientProvider.apiClients.requestController.getDataRequests(
              undefined,
              undefined,
              undefined,
              undefined,
              undefined,
              undefined,
              undefined,
              undefined,
              companyId,
              undefined,
              undefined
            );
            return response.data;
          } catch (error) {
            console.error(`Error fetching data for companyId ${companyId}:`, error);
            return [];
          }
        });
        this.storedDataRequests = (await Promise.all(dataRequestsPromises)).flat();
      }
      this.waitingForData = false;
    },

    /**
     * Filterfunction for frameworks
     * @param framework dataland framework
     * @returns checks if given framework is selected
     */
    filterFramework(framework: string) {
      for (const selectedFramework of this.selectedFrameworks) {
        if (framework == selectedFramework.frameworkDataType) return true;
      }
      return false;
    },
    /**
     * Filterfunction for access status
     * @param accessStatus dataland framework
     * @returns checks if given accessStatus is selected
     */
    filterAccessStatus(accessStatus: string) {
      for (const selectedAccessStatus of this.selectedAccessStatus) {
        if (accessStatus == selectedAccessStatus.displayName) return true;
      }
      return false;
    },
    /**
     * Filterfunction for searchbar
     * @param requesterMail dataland requesterMail
     * @returns checks if given requesterMail contains searchbar text
     */
    filterSearchInput(requesterMail: string | undefined) {
      const lowerCaseRequesterMail = (requesterMail ?? '').toLowerCase();
      const lowerCaseSearchString = this.searchBarInputFilter.toLowerCase();
      return lowerCaseRequesterMail.includes(lowerCaseSearchString);
    },
    /**
     * Resets selected frameworks and searchBarInput
     */
    resetFilterAndSearchBar() {
      this.selectedFrameworks = [];
      this.selectedAccessStatus = [];
      this.searchBarInput = '';
    },
    /**
     * Updates the displayedData
     */
    updateCurrentDisplayedData() {
      this.displayedData = this.storedDataRequests.filter((dataRequest) =>
        this.filterSearchInput(dataRequest.companyName)
      );
      if (this.selectedFrameworks.length > 0) {
        this.displayedData = this.displayedData.filter((dataRequest) => this.filterFramework(dataRequest.dataType));
      }
      if (this.selectedAccessStatus.length > 0) {
        this.displayedData = this.displayedData.filter((dataRequest) =>
          this.filterAccessStatus(dataRequest.accessStatus)
        );
      }
      this.displayedData.sort((a, b) => this.customCompareForStoredDataRequests(a, b));
      this.numberOfFilteredRequests = this.displayedData.length;
      this.displayedData = this.displayedData.slice(
        this.datasetsPerPage * this.currentPage,
        this.datasetsPerPage * (1 + this.currentPage)
      );
      window.scrollTo({
        top: 0,
        behavior: 'smooth',
      });
    },
    /**
     * Compares two  stored data requests (sort field, request status, last modified, requester mail)
     * @param a StoredDataRequest to sort
     * @param b StoredDataRequest to sort
     * @returns result of the comparison
     */
    customCompareForStoredDataRequests(a: ExtendedStoredDataRequest, b: ExtendedStoredDataRequest) {
      const aValue = a[this.sortField] ?? '';
      const bValue = b[this.sortField] ?? '';

      if (this.sortField != ('requestStatus' as keyof ExtendedStoredDataRequest)) {
        if (aValue < bValue) return -1 * this.sortOrder;
        if (aValue > bValue) return this.sortOrder;
      }

      if (a.requestStatus != b.requestStatus)
        return customCompareForRequestStatus(a.requestStatus, b.requestStatus, this.sortOrder);

      if (a.lastModifiedDate < b.lastModifiedDate) return this.sortOrder;
      if (a.lastModifiedDate > b.lastModifiedDate) return -1 * this.sortOrder;

      if ((a.userEmailAddress ?? '') < (b.userEmailAddress ?? '')) return -1 * this.sortOrder;
      else return this.sortOrder;
    },

    /**
     * Updates the access status
     * @param requestId to update
     * @param newAccessStatus to set
     */
    async updateAccessStatus(requestId: string, newAccessStatus: AccessStatus) {
      try {
        if (this.getKeycloakPromise) {
          await new ApiClientProvider(this.getKeycloakPromise()).apiClients.requestController.patchDataRequest(
            requestId,
            { accessStatus: newAccessStatus }
          );
          await this.getStoredCompanyRequestDataList();
          this.updateCurrentDisplayedData();
        }
      } catch (error) {
        console.error(error);
      }
    },
  },
});
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
