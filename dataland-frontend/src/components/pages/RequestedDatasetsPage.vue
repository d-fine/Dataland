<template>
  <AuthenticationWrapper>
    <TheHeader />
    <DatasetsTabMenu :initial-tab-index="3">
      <TheContent class="min-h-screen paper-section relative">
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
                placeholder="Search by company name"
                class="w-12 pl-6 pr-6"
              />
              <i v-if="waitingForSearchResults" class="pi pi-search pl-3 pr-3" aria-hidden="true"></i>
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
            <div class="flex align-items-center">
              <span
                data-test="reset-filter"
                style="margin: 15px"
                class="ml-3 cursor-pointer text-primary font-semibold d-letters"
                @click="resetFilters"
                >RESET</span
              >
            </div>
          </span>
        </div>
        <div class="col-12 text-left p-3">
          <div class="card">
            <DataTable
              :value="displayedData"
              class="table-cursor"
              :rowHover="true"
              data-test="requested-Datasets-table"
              paginator
              paginator-position="bottom"
              :rows="datasetsPerPage"
              lazy
              :total-records="numberOfFilteredRequests"
              @page="onPage($event)"
              @sort="onSort($event)"
            >
              <Column header="COMPANY" class="d-bg-white w-2 qa-review-id" field="companyName" :sortable="true">
                <template #body="slotProps">
                  {{ slotProps.data.companyName }}
                </template>
              </Column>
              <Column header="FRAMEWORK" class="d-bg-white w-2 qa-review-framework" :sortable="true" field="dataType">
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
                  <div>
                    {{ convertDateStringToDate(slotProps.data.creationTimestamp) }}
                  </div>
                  <div style="color: gray; font-size: smaller; line-height: 0.5">
                    <br />
                    {{ convertDateStringToTime(slotProps.data.creationTimestamp) }}
                  </div></template
                >
              </Column>
              <Column
                header="LAST UPDATED"
                class="d-bg-white w-2 qa-review-submission-date"
                :sortable="true"
                field="lastModifiedDate"
              >
                <template #body="slotProps"
                  ><div>
                    {{ convertDateStringToDate(slotProps.data.lastModifiedDate) }}
                  </div>
                  <div style="color: gray; font-size: smaller; line-height: 0.5">
                    <br />
                    {{ convertDateStringToTime(slotProps.data.lastModifiedDate) }}
                  </div>
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
                      data-test="requested-Datasets-Resolve"
                      @click="goToResolveDataRequestViewPage(slotProps.data.datalandCompanyId, slotProps.data.dataType)"
                      >RESOLVE</span
                    >
                    <span class="ml-3">></span>
                  </div>
                </template>
              </Column>
            </DataTable>
          </div>
        </div>
      </TheContent>
    </DatasetsTabMenu>
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
import DataTable, { type DataTablePageEvent, type DataTableSortEvent } from "primevue/datatable";
import Column from "primevue/column";
import { humanizeStringOrNumber } from "@/utils/StringFormatter";
import DatasetsTabMenu from "@/components/general/DatasetsTabMenu.vue";
import { convertUnixTimeInMsToDateString } from "@/utils/DataFormatUtils";
import { type ExtendedStoredDataRequest, RequestStatus } from "@clients/communitymanager";
import { DataTypeEnum } from "@clients/backend";
import InputText from "primevue/inputtext";
import FrameworkDataSearchDropdownFilter from "@/components/resources/frameworkDataSearch/FrameworkDataSearchDropdownFilter.vue";
import type { FrameworkSelectableItem } from "@/utils/FrameworkDataSearchDropDownFilterTypes";
import { ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE } from "@/utils/Constants";
import { getFrontendFrameworkDefinition } from "@/frameworks/FrontendFrameworkRegistry";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";

export default defineComponent({
  name: "RequestedDatasetsPage",
  computed: {
    RequestStatus() {
      return RequestStatus;
    },
  },
  components: {
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
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },

  data() {
    const content: Content = contentData;
    const footerPage: Page | undefined = content.pages.find((page) => page.url === "/");
    const footerContent = footerPage?.sections;
    return {
      waitingForData: true,
      currentPage: 0,
      storedDataRequests: [] as ExtendedStoredDataRequest[],
      displayedData: [] as ExtendedStoredDataRequest[],
      footerContent,
      waitingForSearchResults: true,
      searchBarInput: "",
      searchBarInputFilter: "",
      availableFrameworks: [] as Array<FrameworkSelectableItem>,
      selectedFrameworks: [] as Array<FrameworkSelectableItem>,
      numberOfFilteredRequests: 0,
    };
  },
  mounted() {
    this.availableFrameworks = this.retrieveAvailableFrameworks();
    this.getStoredRequestDataList().catch((error) => console.log(error));
    this.resetFilters();
  },
  watch: {
    selectedFrameworks() {
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
    /**
     * Navigates to the company cockpit view page
     * @param companyId Dataland companyId
     * @param framework Dataland framework
     * @returns the promise of the router push action
     */
    goToResolveDataRequestViewPage(companyId: string, framework: DataTypeEnum) {
      const qaUri = `/companies/${companyId}/frameworks/${framework}`;
      return this.$router.push(qaUri);
    },
    /**
     * Gets list with all available frameworks
     * @returns array of frameworkSelectableItem
     */
    retrieveAvailableFrameworks(): Array<FrameworkSelectableItem> {
      return ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE.map((dataTypeEnum) => {
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
    /**
     * Gets list of storedDataRequests
     */
    async getStoredRequestDataList() {
      this.waitingForData = true;
      this.storedDataRequests = [];
      try {
        if (this.getKeycloakPromise) {
          this.storedDataRequests = (
            await new ApiClientProvider(
              this.getKeycloakPromise(),
            ).apiClients.requestController.getDataRequestsForRequestingUser()
          ).data;
        }
      } catch (error) {
        console.error(error);
      }
      this.waitingForData = false;
    },
    /**
     * Return the title of a framework
     * @param framework dataland framework
     * @returns title of framework
     */
    getFrameworkTitle(framework: DataTypeEnum) {
      switch (framework) {
        case DataTypeEnum.EutaxonomyFinancials:
          return "EU Taxonomy";
        case DataTypeEnum.EutaxonomyNonFinancials:
          return "EU Taxonomy";
        case DataTypeEnum.P2p:
          return "WWF";
        case DataTypeEnum.EsgQuestionnaire:
          return "ESG Questionnaire";
        default:
          return humanizeStringOrNumber(framework);
      }
    },
    /**
     * Return the subtitle of a framework
     * @param framework dataland framework
     * @returns subtitle of framework
     */
    getFrameworkSubtitle(framework: DataTypeEnum) {
      switch (framework) {
        case DataTypeEnum.EutaxonomyFinancials:
          return "for financial companies";
        case DataTypeEnum.EutaxonomyNonFinancials:
          return "for non-financial companies";
        case DataTypeEnum.P2p:
          return "Pathways to Paris";
        case DataTypeEnum.EsgQuestionnaire:
          return "für Corporate Schuldscheindarlehen";
        default:
          return "";
      }
    },
    /**
     * Checks the existence of subtitle for framework
     * @param framework dataland framework
     * @returns boolean if framework has subtitle
     */
    frameworkHasSubTitle(framework: DataTypeEnum) {
      return (
        framework == DataTypeEnum.P2p ||
        framework == DataTypeEnum.EutaxonomyFinancials ||
        framework == DataTypeEnum.EutaxonomyNonFinancials ||
        framework == DataTypeEnum.EsgQuestionnaire
      );
    },
    /**
     * Sorts the list of storedDataRequests
     * @param event contains column to sort and sortOrder
     */
    onSort(event: DataTableSortEvent) {
      const sortField = event.sortField as keyof ExtendedStoredDataRequest;
      const sortOrder = event.sortOrder ?? 1;
      this.storedDataRequests.sort((a, b) => {
        const aValue = a[sortField];
        const bValue = b[sortField];
        return (aValue < bValue ? -1 : 1) * sortOrder;
      });
      this.updateCurrentDisplayedData();
    },
    /**
     * Defines the color of p-badge
     * @param requestStatus status of a request
     * @returns p-badge class
     */
    badgeClass(requestStatus: RequestStatus): string {
      switch (requestStatus) {
        case "Answered":
          return "p-badge badge-blue outline rounded";
        case "Open":
          return "p-badge badge-yellow outline rounded";
        case "Closed":
          return "p-badge badge-light-green outline rounded";
      }
    },
    /**
     * Filterfunction for frameworks
     * @param framework dataland framework
     * @returns checks if given framework is selected
     */
    filterFramework(framework: DataTypeEnum) {
      for (const selectedFramework of this.selectedFrameworks) {
        if (framework == selectedFramework.frameworkDataType) return true;
      }
      return false;
    },
    /**
     * Filterfunction for searchbar
     * @param companyName dataland companyName
     * @returns checks if given companyName contains searchbar text
     */
    filterSearchInput(companyName: string) {
      const lowerCaseCompanyName = (companyName ?? "").toLowerCase();
      const lowerCaseSearchString = this.searchBarInputFilter.toLowerCase();
      return lowerCaseCompanyName.includes(lowerCaseSearchString);
    },
    /**
     * Resets selected frameworks
     */
    resetFilters() {
      this.selectedFrameworks = this.availableFrameworks;
    },
    /**
     * Updates the displayedData
     */
    updateCurrentDisplayedData() {
      this.waitingForSearchResults = true;
      this.displayedData = this.storedDataRequests
        .filter((dataRequest) => this.filterSearchInput(dataRequest.companyName))
        .filter((dataRequest) => this.filterFramework(dataRequest.dataType));
      this.numberOfFilteredRequests = this.displayedData.length;
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
    /**
     * Converts dateString to date
     * @param date unix time
     * @returns string representing a date (DD.MM.YYYY)
     */
    convertDateStringToDate(date: number) {
      const parsedDate = new Date(date);

      const day = parsedDate.getDate();
      const month = parsedDate.getMonth() + 1;
      const year = parsedDate.getFullYear();

      const paddedDay = day < 10 ? "0" + day : day;
      const paddedMonth = month < 10 ? "0" + month : month;

      return `${paddedDay}.${paddedMonth}.${year}`;
    },
    /**
     * Converts dateString to time
     * @param date unix time
     * @returns string representing a time (HH:MM)
     */
    convertDateStringToTime(date: number) {
      const dateString = convertUnixTimeInMsToDateString(date);
      return dateString.split(",")[2].trim();
    },
  },
});
</script>
