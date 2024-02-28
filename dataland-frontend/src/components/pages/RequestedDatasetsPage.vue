<!-- todo: unnötige imports, componenten etc entfernen -->
<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="paper-section flex">
      <DatasetsTabMenu :initial-tab-index="3">
        <AuthorizationWrapper :required-role="KEYCLOAK_ROLE_REVIEWER">
          <div id="searchBarAndFiltersContainer" class="w-full bg-white" ref="searchBarAndFiltersContainer">
            <span class="w-12 p-input-icon-left p-input-icon-right">
              <i class="pi pi-search pl-3 pr-3" aria-hidden="true" style="color: #958d7c" />
              <InputText
                v-model="searchBarInput"
                placeholder="Search requests by company name"
                class="w-12 pl-6 pr-6"
              />
              <i v-if="waitingForSearchResults" class="pi pi-spin pi-spinner right-0 mr-3" aria-hidden="true"></i>
              <FrameworkDataSearchDropdownFilter
                v-model="selectedFrameworksInt"
                ref="frameworkFilter"
                :available-items="availableFrameworks"
                filter-name="Framework"
                filter-id="framework-filter"
                filter-placeholder="Search frameworks"
                class="ml-3"
              />
            </span>
          </div>
          <div class="col-12 text-left p-3">
            <div class="card">
              <DataTable
                :value="displayedData"
                class="table-cursor"
                id="qa-data-result"
                :rowHover="true"
                data-test="qa-review-section"
                paginator
                paginator-position="bottom"
                :rows="datasetsPerPage"
                lazy
                :total-records="storedDataRequest.length"
                @page="onPage($event)"
                @sort="onSort($event)"
              >
                <Column
                  header="COMPANY"
                  class="d-bg-white w-2 qa-review-id"
                  field="dataRequestCompanyIdentifierValue"
                  :sortable="true"
                >
                  <template #body="slotProps">
                    {{ slotProps.data.dataRequestCompanyIdentifierValue }}
                  </template>
                </Column>
                <Column header="FRAMEWORK" class="d-bg-white w-2 qa-review-framework" :sortable="true" field="dataType">
                  <template #body="slotProps">
                    {{ humanizeString(slotProps.data.dataType) }}
                  </template>
                </Column>
                <Column
                  header="YEAR"
                  class="d-bg-white w-2 qa-review-company-name"
                  field="reportingPeriod"
                  :sortable="true"
                >
                  <template #body="slotProps">
                    {{ slotProps.data.reportingPeriod }}
                  </template>
                </Column>
                <Column
                  header="REQUESTED DATE"
                  class="d-bg-white w-2 qa-review-reporting-period"
                  field="creationTimestamp"
                  :sortable="true"
                >
                  <template #body="slotProps">
                    {{ convertUnixTimeInMsToDateString(slotProps.data.creationTimestamp) }}
                  </template>
                </Column>
                <Column
                  header="LAST UPDATED"
                  class="d-bg-white w-2 qa-review-submission-date"
                  :sortable="true"
                  field="lastModifiedDate"
                >
                  <template #body="slotProps">
                    {{ convertUnixTimeInMsToDateString(slotProps.data.lastModifiedDate) }}
                  </template>
                </Column>
                <Column
                  header="STATUS"
                  class="d-bg-white w-2 qa-review-submission-date"
                  :sortable="true"
                  field="requestStatus"
                >
                  <template #body="slotProps">
                    <div :class="badgeClass(slotProps.data.requestStatus)" style="display: inline-flex">
                      {{ slotProps.data.requestStatus }}
                    </div>
                  </template>
                </Column>
                <Column field="resolve" header="" class="w-2 d-bg-white qa-review-button">
                  <template #body="slotProps">
                    <div
                      v-if="slotProps.data.requestStatus == RequestStatus.Answered"
                      class="text-right text-primary no-underline font-bold"
                    >
                      <span
                        @click="
                          () =>
                            goToResolveDataRequestViewPage(
                              slotProps.data.dataRequestCompanyIdentifierType,
                              slotProps.data.dataType,
                            )
                        "
                        >RESOLVE</span
                      >
                      <span class="ml-3">></span>
                    </div>
                  </template>
                </Column>
              </DataTable>
            </div>
          </div>
        </AuthorizationWrapper>
      </DatasetsTabMenu>
    </TheContent>
    <TheFooter :is-light-version="true" :sections="footerContent" />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import TheFooter from "@/components/generics/TheNewFooter.vue";
import contentData from "@/assets/content.json";
import type { Content, Page } from "@/types/ContentTypes";
import TheContent from "@/components/generics/TheContent.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import { defineComponent, inject, ref } from "vue";
import type Keycloak from "keycloak-js";
import { ApiClientProvider } from "@/services/ApiClients";
import AuthorizationWrapper from "@/components/wrapper/AuthorizationWrapper.vue";
import { KEYCLOAK_ROLE_REVIEWER } from "@/utils/KeycloakUtils";
import DataTable, { type DataTablePageEvent, type DataTableSortEvent } from "primevue/datatable";
import Column from "primevue/column";
import { humanizeStringOrNumber } from "@/utils/StringFormatter";
import DatasetsTabMenu from "@/components/general/DatasetsTabMenu.vue";
import { convertUnixTimeInMsToDateString } from "@/utils/DataFormatUtils";
import {
  type DataRequestCompanyIdentifierType,
  RequestStatus,
  type StoredDataRequest,
} from "@clients/communitymanager";
import { type DataTypeEnum } from "@clients/backend";
import InputText from "primevue/inputtext";
import FrameworkDataSearchDropdownFilter from "@/components/resources/frameworkDataSearch/FrameworkDataSearchDropdownFilter.vue";
import type { FrameworkSelectableItem } from "@/utils/FrameworkDataSearchDropDownFilterTypes";
import { ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE } from "@/utils/Constants";
import { getFrontendFrameworkDefinition } from "@/frameworks/FrontendFrameworkRegistry";

export default defineComponent({
  name: "RequestedDatasetsPage",
  computed: {
    RequestStatus() {
      return RequestStatus;
    },
    selectedFrameworksInt: {
      get(): Array<FrameworkSelectableItem> {
        return this.availableFrameworks.filter((frameworkSelectableItem) =>
          this.selectedFrameworks.includes(frameworkSelectableItem.frameworkDataType),
        );
      },
      set(newValue: Array<FrameworkSelectableItem>) {
        this.$emit(
          "update:selectedFrameworks",
          newValue.map((frameworkSelectableItem) => frameworkSelectableItem.frameworkDataType),
        );
      },
    },
  },
  components: {
    FrameworkDataSearchDropdownFilter,
    DatasetsTabMenu,
    AuthorizationWrapper,
    TheFooter,
    TheContent,
    TheHeader,
    DataTable,
    Column,
    InputText,
  },
  emits: ["update:selectedFrameworks"],
  props: {
    selectedFrameworks: {
      type: Array as () => Array<DataTypeEnum>,
      default: () => [],
    },
  },
  setup() {
    return {
      frameworkFilter: ref(),
      datasetsPerPage: 100,
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },

  data() {
    const content: Content = contentData;
    const footerPage: Page | undefined = content.pages.find((page) => page.url === "/");
    const footerContent = footerPage?.sections;
    return {
      waitingForData: true,
      KEYCLOAK_ROLE_REVIEWER,
      currentPage: 0,
      storedDataRequest: [] as StoredDataRequest[],
      displayedData: [] as StoredDataRequest[],
      footerContent,
      waitingForSearchResults: true,
      searchBarInput: "",
      searchBarInputFilter: "",
      availableFrameworks: [] as Array<FrameworkSelectableItem>,
    };
  },
  mounted() {
    this.retrieveAvailableFrameworks();
    this.getStoredRequestData().catch((error) => console.log(error));
    this.updateCurrentDisplayedData();
    console.log(this.availableFrameworks);
  },
  watch: {
    searchBarInput(newSearch: string) {
      this.searchBarInputFilter = newSearch;
      this.updateCurrentDisplayedData();
    },
  },
  methods: {
    convertUnixTimeInMsToDateString,
    humanizeString: humanizeStringOrNumber,
    /**
     * Navigates to the company cockpit view page
     * @param event the row click event
     * @param companyId
     * @param framework
     * @returns the promise of the router push action
     */
    goToResolveDataRequestViewPage(companyId: DataRequestCompanyIdentifierType, framework: DataTypeEnum) {
      const qaUri = `/companies/${companyId}/frameworks/${framework}`;
      return this.$router.push(qaUri);
    },

    retrieveAvailableFrameworks() {
      this.availableFrameworks = ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE.map((dataTypeEnum) => {
        let displayName = humanizeStringOrNumber(dataTypeEnum);
        const frameworkDefinition = getFrontendFrameworkDefinition(dataTypeEnum);
        if (frameworkDefinition) {
          displayName = frameworkDefinition.label;
        }
        return {
          frameworkDataType: dataTypeEnum,
          displayName: displayName,
          disabled: false,
        };
      });
    },
    async getStoredRequestData() {
      try {
        this.waitingForData = true;
        this.storedDataRequest = [];
        if (this.getKeycloakPromise) {
          this.storedDataRequest = (
            await new ApiClientProvider(this.getKeycloakPromise()).apiClients.requestController.getDataRequestsForUser()
          ).data;
        }
      } catch (error) {
        console.error(error);
      }

      this.storedDataRequest = [];
      for (let i = 0; i < 20; i++) {
        this.storedDataRequest.push({
          dataRequestId: "abcd" + i,
          dataRequestCompanyIdentifierValue: "company" + i,
          dataType: "lksg",
          reportingPeriod: "2021",
          creationTimestamp: 10 + i,
          lastModifiedDate: 11 + i,
          requestStatus: "Open",
        } as StoredDataRequest);
      }
      for (let i = 0; i < 20; i++) {
        this.storedDataRequest.push({
          dataRequestId: "abcde" + i,
          dataRequestCompanyIdentifierValue: "companyXX" + i,
          dataType: "sme",
          reportingPeriod: "2020",
          creationTimestamp: 10 + i,
          lastModifiedDate: 11 + i,
          requestStatus: "Answered",
        } as StoredDataRequest);
      }
      this.waitingForData = false;
      this.waitingForSearchResults = false;
    },
    onSort(event: DataTableSortEvent) {
      const sortField = event.sortField;
      const sortOrder = event.sortOrder;
      this.storedDataRequest.sort((a, b) => {
        const aValue = a[sortField];
        const bValue = b[sortField];
        return (aValue < bValue ? -1 : 1) * sortOrder;
      });
      this.updateCurrentDisplayedData();
    },
    badgeClass(requestStatus: RequestStatus) {
      switch (requestStatus) {
        case "Answered":
          return "p-badge badge-blue outline rounded";
        case "Open":
          return "p-badge badge-yellow outline rounded";
        case "Closed":
          return "p-badge badge-light-green outline rounded";
      }
    },
    filterFramework(dataRequest: StoredDataRequest) {
      return true;
    },
    filterSearchInput(companyName: string) {
      const lowerCaseSearchString = this.searchBarInput.toLowerCase();
      if (companyName.toLowerCase().includes(lowerCaseSearchString)) {
        return true;
      } else {
        return false;
      }
    },
    updateCurrentDisplayedData() {
      this.waitingForSearchResults = true;
      this.displayedData = this.storedDataRequest
        .filter((dataRequest) => this.filterSearchInput(dataRequest.dataRequestCompanyIdentifierValue)) //todo Map companie Id to Human readible
        .filter((dataRequest) => this.filterFramework(dataRequest)); //todo
      this.displayedData = this.displayedData.slice(
        this.datasetsPerPage * this.currentPage,
        this.datasetsPerPage * (1 + this.currentPage),
      );
      this.waitingForSearchResults = false;
      window.scrollTo({
        top: 0,
        behavior: "smooth",
      });
    },
    /**
     * Updates the data for the current page
     * @param event event containing the new page
     */
    onPage(event: DataTablePageEvent) {
      this.currentPage = event.page;
      this.updateCurrentDisplayedData();
    },
  },
});
</script>

<style>
#qa-data-result tr:hover {
  cursor: pointer;
}
</style>
