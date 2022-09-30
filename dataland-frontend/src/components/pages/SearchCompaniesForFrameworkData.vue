<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="pl-0 pt-0 min-h-screen surface-800 relative">
      <div
        class="col-12 bg-white"
        :class="[searchBarToggled && pageScrolled ? ['d-search-toggle', 'fixed'] : '']"
        ref="searchBarContainer"
      >
        <div class="pt-4" />
        <MarginWrapper>
          <FrameworkDataSearchBar
            v-model="currentSearchBarInput"
            ref="frameworkDataSearchBar"
            :searchBarName="searchBarName"
            :frameworks-to-filter-for="currentFilteredFrameworks"
            :country-codes-to-filter-for="currentFilteredCountryCodes"
            :sectors-to-filter-for="currentFilteredSectors"
            :enable-full-search="true"
            @companies-received="handleCompanyQuery"
          />
          <div class="flex">
            <FrameworkDataSearchFilters
              :show-heading="true"
              v-model:selected-country-codes="currentFilteredCountryCodes"
              v-model:selected-frameworks="currentFilteredFrameworks"
              v-model:selected-sectors="currentFilteredSectors"
            />
            <span class="d-page-display">{{ currentlyVisiblePageText }}</span>
          </div>
          <div
            :class="[
              pageScrolled && !searchBarToggled
                ? ['col-12', 'align-items-center', 'grid', 'bg-white', 'd-search-toggle', 'fixed', 'd-shadow-bottom']
                : 'pl-2',
            ]"
          >
            <template v-if="!searchBarToggled && pageScrolled">
              <span class="mr-3 font-semibold">Search Data for Companies</span>
              <PrimeButton
                name="search_bar_collapse"
                icon="pi pi-search"
                class="p-button-rounded surface-ground border-none m-2"
                @click="toggleSearchBar"
              >
                <i class="pi pi-search" aria-hidden="true" style="z-index: 20; color: #958d7c" />
              </PrimeButton>
              <FrameworkDataSearchFilters
                class="ml-3"
                :show-heading="false"
                v-model:selected-country-codes="currentFilteredCountryCodes"
                v-model:selected-frameworks="currentFilteredFrameworks"
                v-model:selected-sectors="currentFilteredSectors"
              />
            </template>
          </div>
        </MarginWrapper>
      </div>
      <FrameworkDataSearchResults
        ref="searchResults"
        v-if="showSearchResultsTable"
        :data="resultsArray"
        @update:first="setFirstShownRow"
      />
    </TheContent>
  </AuthenticationWrapper>
</template>

<style scoped>
.d-shadow-bottom {
  box-shadow: 0px 4px 4px rgba(0, 0, 0, 0.02);
}

.d-page-display {
  margin-left: auto;
  margin-top: auto;
  margin-bottom: 0;
  color: #5a4f36;
}
</style>

<script lang="ts">
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import FrameworkDataSearchBar from "@/components/resources/frameworkDataSearch/FrameworkDataSearchBar.vue";
import PrimeButton from "primevue/button";
import FrameworkDataSearchResults from "@/components/resources/frameworkDataSearch/FrameworkDataSearchResults.vue";
import { useRoute } from "vue-router";
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";
import { defineComponent, ref } from "vue";
import { DataTypeEnum } from "@clients/backend";
import { DataSearchStoredCompany } from "@/utils/SearchCompaniesForFrameworkDataPageDataRequester";
import FrameworkDataSearchFilters from "@/components/resources/frameworkDataSearch/FrameworkDataSearchFilters.vue";
import { parseQueryParamArray } from "@/utils/QueryParserUtils";

export default defineComponent({
  name: "SearchCompaniesForFrameworkData",
  components: {
    FrameworkDataSearchFilters,
    MarginWrapper,
    AuthenticationWrapper,
    TheHeader,
    TheContent,
    FrameworkDataSearchBar,
    PrimeButton,
    FrameworkDataSearchResults,
  },

  created() {
    window.addEventListener("scroll", this.handleScroll);
    this.scanQueryParams();
  },
  data() {
    return {
      searchBarToggled: false,
      pageScrolled: false,
      route: useRoute(),
      showSearchResultsTable: false,
      resultsArray: [] as Array<DataSearchStoredCompany>,
      latestScrollPosition: 0,
      currentSearchBarInput: "",
      currentFilteredFrameworks: [] as Array<DataTypeEnum>,
      currentFilteredCountryCodes: [] as Array<string>,
      currentFilteredSectors: [] as Array<string>,
      scrollEmittedByToggleSearchBar: false,
      hiddenSearchBarHeight: 0,
      searchBarName: "search_bar_top",
      firstShownRow: 0,
    };
  },
  setup() {
    return {
      frameworkDataSearchBar: ref(),
      searchBarContainer: ref(),
      searchResults: ref(),
    };
  },
  watch: {
    pageScrolled(pageScrolledNew) {
      if (pageScrolledNew) {
        this.frameworkDataSearchBar.$refs.autocomplete.hideOverlay();
      }
      if (!pageScrolledNew) {
        this.searchBarToggled = false;
      }
    },
  },
  computed: {
    currentlyVisiblePageText(): string {
      const totalSearchResults = this.resultsArray.length;

      if (totalSearchResults <= 0) return "No results";

      const startIndex = this.firstShownRow;
      const endIndex = startIndex + 99 >= totalSearchResults ? totalSearchResults - 1 : startIndex + 99;
      return `${startIndex + 1}-${endIndex + 1} of ${totalSearchResults}`;
    },
  },
  methods: {
    setFirstShownRow(value: number) {
      this.firstShownRow = value;
    },
    handleScroll() {
      const windowScrollY = window.scrollY;
      if (this.scrollEmittedByToggleSearchBar) {
        this.scrollEmittedByToggleSearchBar = false;
      } else {
        if (this.searchBarToggled) {
          this.searchBarToggled = false;
          this.searchBarName = "search_bar_top";
          window.scrollBy(0, this.hiddenSearchBarHeight);
        }
        if (this.latestScrollPosition > windowScrollY) {
          //ScrollUP event
          this.latestScrollPosition = windowScrollY;
          this.pageScrolled = document.documentElement.scrollTop >= 50;
        } else {
          //ScrollDOWN event
          this.pageScrolled = document.documentElement.scrollTop > 80;
          this.latestScrollPosition = windowScrollY;
        }
      }
    },
    scanQueryParams() {
      let queryFrameworks = this.route.query.framework;
      if (queryFrameworks !== undefined) {
        const allowedDataTypeEnumValues = Object.values(DataTypeEnum) as Array<string>;
        this.currentFilteredFrameworks = parseQueryParamArray(queryFrameworks).filter((it) =>
          allowedDataTypeEnumValues.includes(it)
        ) as Array<DataTypeEnum>;
      } else {
        this.currentFilteredFrameworks = Object.values(DataTypeEnum);
      }

      let queryCountryCodes = this.route.query.countryCode;
      if (queryCountryCodes) {
        this.currentFilteredCountryCodes = parseQueryParamArray(queryCountryCodes);
      }

      let querySectors = this.route.query.sector;
      if (querySectors) {
        this.currentFilteredSectors = parseQueryParamArray(querySectors);
      }

      let queryInput = this.route.query.input as string;
      if (queryInput) {
        this.currentSearchBarInput = queryInput;
      }
    },
    handleCompanyQuery(companiesReceived: Array<DataSearchStoredCompany>) {
      this.resultsArray = companiesReceived;
      this.showSearchResultsTable = true;

      const queryInput = this.currentSearchBarInput == "" ? undefined : this.currentSearchBarInput;

      const allFrameworksSelected = Object.values(DataTypeEnum).every((it) =>
        this.currentFilteredFrameworks.includes(it)
      );
      let queryFrameworks: DataTypeEnum[] | undefined | null = this.currentFilteredFrameworks;
      if (allFrameworksSelected) queryFrameworks = undefined;
      if (this.currentFilteredFrameworks.length == 0) queryFrameworks = null;

      const queryCountryCodes =
        this.currentFilteredCountryCodes.length == 0 ? undefined : this.currentFilteredCountryCodes;

      const querySectors = this.currentFilteredSectors.length == 0 ? undefined : this.currentFilteredSectors;
      this.searchResults?.resetPagination();
      this.$router.push({
        name: "Search Companies for Framework Data",
        query: {
          input: queryInput,
          framework: queryFrameworks,
          countryCode: queryCountryCodes,
          sector: querySectors,
        },
      });
    },
    toggleSearchBar() {
      this.searchBarToggled = true;
      const height = this.searchBarContainer.clientHeight;
      window.scrollBy(0, -height);
      this.hiddenSearchBarHeight = height;
      this.scrollEmittedByToggleSearchBar = true;
      this.searchBarName = "search_bar_scrolled";
    },
    unmounted() {
      window.removeEventListener("scroll", this.handleScroll);
    },
  },
});
</script>
