<template>
  <AuthenticationWrapper>
    <TheHeader />
    <DatasetsTabMenu :initial-tab-index="4">
      <TheContent class="min-h-screen paper-section relative">
        <div v-if="waitingForData || storedDataRequests.length > 0">
          <div
            id="searchBarAndFiltersContainer"
            class="w-full bg-white pt-4 justify-between"
            ref="searchBarAndFiltersContainer"
          >
            <span class="align-content-start flex items-center justify-start">
              <span class="w-3 p-input-icon-left" style="margin: 15px">
                <i class="pi pi-search pl-3 pr-3" aria-hidden="true" style="color: #958d7c" />
                <InputText
                  data-test="requested-Datasets-searchbar"
                  v-model="searchBarInput"
                  placeholder="Search by requester"
                  class="w-12 pl-6 pr-6"
                />
              </span>
              <FrameworkDataSearchDropdownFilter
                v-model="selectedFrameworks"
                ref="frameworkFilter"
                :available-items="availableFrameworks"
                filter-name="Framework"
                data-test="requested-Datasets-frameworks"
                filter-id="framework-filter"
                filter-placeholder="Search frameworks"
                class="ml-3"
                style="margin: 15px"
              />
              <FrameworkDataSearchDropdownFilter
                v-model="selectedAccessStatus"
                ref="frameworkFilter"
                :available-items="availableAccessStatus"
                filter-name="Access Status"
                data-test="requested-Datasets-frameworks"
                filter-id="framework-filter"
                filter-placeholder="access status"
                class="ml-3"
                style="margin: 15px"
              />
              <div class="flex align-items-center">
                <span
                  data-test="reset-filter"
                  style="margin: 15px"
                  class="ml-3 cursor-pointer text-primary font-semibold d-letters"
                  @click="resetFilterAndSearchBar"
                  >RESET</span
                >
              </div>
            </span>
          </div>
          <div class="col-12 text-left p-3">
            <div class="card">
              <DataTable
                :value="displayedData"
                style="cursor: pointer"
                :rowHover="true"
                :loading="waitingForData"
                data-test="requested-Datasets-table"
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
                    </div></template
                  >
                </Column>
                <Column header="LAST UPDATED" :sortable="true" field="lastModifiedDate">
                  <template #body="slotProps"
                    ><div>
                      {{ convertUnixTimeInMsToDateString(slotProps.data.lastModifiedDate) }}
                    </div>
                  </template>
                </Column>
                <Column header="REQUEST STATUS" :sortable="true" field="requestStatus">
                  <template #body="slotProps">
                    <div :class="badgeClass(slotProps.data.requestStatus)" style="display: inline-flex">
                      {{ slotProps.data.requestStatus }}
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
                          class="uppercase p-button p-button-sm"
                          @click="updateAccessStatus(slotProps.data.dataRequestId, AccessStatus.Granted)"
                        >
                          <i class="material-icons"> done </i>
                          <span class="d-letters pl-2"> Grant </span>
                        </PrimeButton>
                        <PrimeButton
                          class="uppercase p-button-outlined p-button-sm mr-3"
                          @click="updateAccessStatus(slotProps.data.dataRequestId, AccessStatus.Declined)"
                        >
                          <i class="material-icons"> clear </i>
                          <span class="d-letters pl-2"> Decline </span>
                        </PrimeButton>
                      </div>
                    </div>
                    <div
                      v-if="slotProps.data.accessStatus == AccessStatus.Granted"
                      class="text-right text-primary no-underline font-bold"
                    >
                      <div class="button-container">
                        <PrimeButton
                          class="uppercase p-button-outlined p-button-sm mr-3"
                          @click="updateAccessStatus(slotProps.data.dataRequestId, AccessStatus.Revoked)"
                        >
                          <i class="material-icons"> clear </i>
                          <span class="d-letters pl-2"> Revoke </span>
                        </PrimeButton>
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
    </DatasetsTabMenu>
    <TheFooter :is-light-version="true" :sections="footerContent" />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import TheFooter from '@/components/generics/TheNewFooter.vue';
import contentData from '@/assets/content.json';
import type { Content, Page } from '@/types/ContentTypes';
import TheContent from '@/components/generics/TheContent.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import { defineComponent, inject, ref } from 'vue';
import type Keycloak from 'keycloak-js';
import { ApiClientProvider } from '@/services/ApiClients';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import { frameworkHasSubTitle, getFrameworkSubtitle, getFrameworkTitle } from '@/utils/StringFormatter';
import DatasetsTabMenu from '@/components/general/DatasetsTabMenu.vue';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import { type CompanyRoleAssignment, AccessStatus, type StoredDataRequest } from '@clients/communitymanager';
import InputText from 'primevue/inputtext';
import FrameworkDataSearchDropdownFilter from '@/components/resources/frameworkDataSearch/FrameworkDataSearchDropdownFilter.vue';
import { type FrameworkSelectableItem, type SelectableItem } from '@/utils/FrameworkDataSearchDropDownFilterTypes';
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import { accessStatusBadgeClass, badgeClass } from '@/utils/RequestUtils';
import PrimeButton from 'primevue/button';
import {
  customCompareForRequestStatus,
  retrieveAvailableAccessStatus,
  retrieveAvailableFrameworks,
} from '@/utils/RequestsOverviewPageUtils';

export default defineComponent({
  name: 'MyDataRequestsOverview',
  computed: {
    AccessStatus() {
      return AccessStatus;
    },
  },
  components: {
    PrimeButton,
    AuthenticationWrapper,
    FrameworkDataSearchDropdownFilter,
    DatasetsTabMenu,
    TheFooter,
    TheContent,
    TheHeader,
    DataTable,
    Column,
    InputText,
  },

  setup() {
    return {
      frameworkFilter: ref(),
      datasetsPerPage: 100,
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
      companyRoleAssignments: inject<Array<CompanyRoleAssignment>>('companyRoleAssignments'),
    };
  },

  data() {
    const content: Content = contentData;
    const footerPage: Page | undefined = content.pages.find((page) => page.url === '/');
    const footerContent = footerPage?.sections;
    return {
      waitingForData: true,
      currentPage: 0,
      storedDataRequests: [] as StoredDataRequest[],
      displayedData: [] as StoredDataRequest[],
      footerContent,
      searchBarInput: '',
      searchBarInputFilter: '',
      availableFrameworks: [] as Array<FrameworkSelectableItem>,
      selectedFrameworks: [] as Array<FrameworkSelectableItem>,
      availableAccessStatus: [] as Array<SelectableItem>,
      selectedAccessStatus: [] as Array<SelectableItem>,
      numberOfFilteredRequests: 0,
      sortField: 'requestStatus' as keyof StoredDataRequest,
      sortOrder: 1,
    };
  },
  mounted() {
    this.availableFrameworks = retrieveAvailableFrameworks();
    this.availableAccessStatus = retrieveAvailableAccessStatus();
    this.getStoredCompanyRequestDataList().catch((error) => console.error(error));
    this.resetFilterAndSearchBar();
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
  },
  methods: {
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
              companyId
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
      this.selectedFrameworks = this.availableFrameworks;
      this.selectedAccessStatus = this.availableAccessStatus;
      this.searchBarInput = '';
    },
    /**
     * Updates the displayedData
     */
    updateCurrentDisplayedData() {
      this.displayedData = this.storedDataRequests
        .filter((dataRequest) => this.filterSearchInput(dataRequest.userEmailAddress))
        .filter((dataRequest) => this.filterFramework(dataRequest.dataType))
        .filter((dataRequest) => this.filterAccessStatus(dataRequest.accessStatus));
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
    customCompareForStoredDataRequests(a: StoredDataRequest, b: StoredDataRequest) {
      const aValue = a[this.sortField] ?? '';
      const bValue = b[this.sortField] ?? '';

      if (this.sortField != ('requestStatus' as keyof StoredDataRequest)) {
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
            undefined,
            newAccessStatus
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
</style>
