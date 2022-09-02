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
            :frameworksToFilterFor="currentFilteredFrameworks"
            @companies-received="handleCompanyQuery"
            @rendered="handleFrameworkDataSearchBarRender"
            class="pl-4"
          />

          <div
            :class="[
              pageScrolled && !searchBarToggled
                ? ['col-12', 'align-items-center', 'grid', 'bg-white', 'd-search-toggle', 'fixed', 'd-shadow-bottom']
                : 'pl-2',
            ]"
          >
            <span v-if="!searchBarToggled && pageScrolled" class="mr-3 font-semibold">Search Data for Companies</span>
            <PrimeButton
              v-if="!searchBarToggled && pageScrolled"
              name="search_bar_collapse"
              icon="pi pi-search"
              class="p-button-rounded surface-ground border-none m-2"
              @click="toggleSearchBar"
            >
              <i class="pi pi-search" aria-hidden="true" style="z-index: 20; color: #958d7c" />
            </PrimeButton>
          </div>
        </MarginWrapper>
      </div>

      <FrameworkDataSearchResults v-if="showSearchResultsTable" :data="resultsArray" />
    </TheContent>
  </AuthenticationWrapper>
</template>

<style scoped>
.d-shadow-bottom {
  box-shadow: 0px 4px 4px rgba(0, 0, 0, 0.02);
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
import { DataTypeEnum } from "../../../build/clients/backend/org/dataland/datalandfrontend/openApiClient/model";
import { DataSearchStoredCompany } from "@/utils/SearchCompaniesForFrameworkDataPageDataRequester";

export default defineComponent({
  name: "SearchCompaniesForFrameworkData",
  components: {
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
      currentFilteredFrameworks: Object.values(DataTypeEnum),
      scrollEmittedByToggleSearchBar: false,
      hiddenSearchBarHeight: 0,
      searchBarName: "search_bar_top",
    };
  },
  setup() {
    return {
      frameworkDataSearchBar: ref(),
      searchBarContainer: ref(),
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

  methods: {
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
    handleFrameworkDataSearchBarRender() {
      let queryFrameworks = this.route.query.frameworks;
      if (queryFrameworks) {
        if (typeof queryFrameworks === "string" && queryFrameworks !== "") {
          this.currentFilteredFrameworks = [queryFrameworks as DataTypeEnum];
        } else if (Array.isArray(queryFrameworks)) {
          this.currentFilteredFrameworks = queryFrameworks as Array<DataTypeEnum>;
        }
      }

      let queryInput = this.route.query.input as string;
      if (queryInput) {
        this.currentSearchBarInput = queryInput;
      }

      this.frameworkDataSearchBar.queryCompany(this.currentSearchBarInput, this.currentFilteredFrameworks);
    },
    handleCompanyQuery(companiesReceived: Array<DataSearchStoredCompany>) {
      this.resultsArray = companiesReceived;
      this.showSearchResultsTable = true;

      const frameworksQuery =
        this.currentFilteredFrameworks && this.currentFilteredFrameworks.length === 0
          ? ""
          : this.currentFilteredFrameworks;

      this.$router.push({
        name: "Search Companies for Framework Data",
        query: { input: this.currentSearchBarInput, frameworks: frameworksQuery },
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
