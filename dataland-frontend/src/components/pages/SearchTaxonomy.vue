<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="pl-0">
      <div
        class="col-12 bg-white"
        :class="[searchBarToggled && pageScrolled ? ['d-search-toggle', 'fixed'] : '']"
        ref="searchBarAndIndexTabContainer"
      >
        <SearchTaxonomyHeader class="pl-4" />
        <MarginWrapper>
          <EuTaxoSearchBar
            v-model="currentInput"
            ref="euTaxoSearchBar"
            :taxoSearchBarName="taxoSearchBarName"
            @companies-received="handleCompanyQuery"
            @rendered="handleEuTaxoSearchBarRender"
            class="pl-4"
          />

          <div
            :class="[
              pageScrolled && !searchBarToggled
                ? ['col-12', 'align-items-center', 'grid', 'bg-white', 'd-search-toggle', 'fixed', 'd-shadow-bottom']
                : 'pl-2',
            ]"
          >
            <span v-if="!searchBarToggled && pageScrolled" class="mr-3 font-semibold">Search EU Taxonomy data</span>
            <PrimeButton
              v-if="!searchBarToggled && pageScrolled"
              name="search_bar_collapse"
              icon="pi pi-search"
              class="p-button-rounded surface-ground border-none m-2"
              @click="toggleSearchBar"
            >
              <i class="pi pi-search" aria-hidden="true" style="z-index: 20; color: #958d7c" />
            </PrimeButton>
            <IndexTabs
              ref="indexTabs"
              :initIndex="firstDisplayedIndex"
              @tab-click="toggleIndexTabs"
              @companies-received="handleFilterByIndex"
            />
          </div>
        </MarginWrapper>
      </div>

      <EuTaxoSearchResults v-if="showSearchResultsTable" :data="resultsArray" />
    </TheContent>
  </AuthenticationWrapper>
</template>

<style scoped>
.d-shadow-bottom {
  box-shadow: 0px 4px 4px rgba(0, 0, 0, 0.02);
}
</style>

<script>
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper";
import TheHeader from "@/components/structure/TheHeader";
import TheContent from "@/components/structure/TheContent";
import SearchTaxonomyHeader from "@/components/resources/taxonomy/search/SearchTaxonomyHeader";
import EuTaxoSearchBar from "@/components/resources/taxonomy/search/EuTaxoSearchBar";
import IndexTabs from "@/components/resources/indices/IndexTabs";
import PrimeButton from "primevue/button";
import EuTaxoSearchResults from "@/components/resources/taxonomy/search/EuTaxoSearchResults";
import { useRoute } from "vue-router";
import apiSpecs from "../../../build/clients/backend/backendOpenApi.json";
import MarginWrapper from "@/components/wrapper/MarginWrapper";

const stockIndices = apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum;

export default {
  name: "SearchTaxonomy",
  components: {
    MarginWrapper,
    AuthenticationWrapper,
    TheHeader,
    TheContent,
    SearchTaxonomyHeader,
    EuTaxoSearchBar,
    IndexTabs,
    PrimeButton,
    EuTaxoSearchResults,
  },

  created() {
    window.addEventListener("scroll", this.handleScroll);
  },
  data() {
    return {
      searchBarToggled: false,
      pageScrolled: false,
      route: useRoute(),
      firstDisplayedIndex: 1,
      showSearchResultsTable: false,
      resultsArray: [],
      latestScrollPosition: 0,
      currentInput: null,
      scrollEmittedByToggleSearchBar: false,
      hiddenSearchBarHeight: 0,
      taxoSearchBarName: "eu_taxonomy_search_bar_top",
    };
  },

  watch: {
    pageScrolled(pageScrolledNew) {
      if (pageScrolledNew) {
        this.$refs.euTaxoSearchBar.$refs.autocomplete.hideOverlay();
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
          this.taxoSearchBarName = "eu_taxonomy_search_bar_top";
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

    handleEuTaxoSearchBarRender() {
      if (this.route.query && this.route.query.input) {
        this.currentInput = this.route.query.input;
        this.$refs.euTaxoSearchBar.queryCompany(this.currentInput);
      } else if (this.route.path === "/search/eutaxonomy") {
        this.$refs.euTaxoSearchBar.$refs.autocomplete.focus();
        this.toggleIndexTabs(stockIndices[this.firstDisplayedIndex], this.firstDisplayedIndex);
      }
    },
    handleCompanyQuery(companiesReceived) {
      this.$refs.indexTabs.activeIndex = null;
      this.resultsArray = companiesReceived;
      this.showSearchResultsTable = true;
      this.$router.push({ name: "Search Eu Taxonomy Data", query: { input: this.currentInput } });
    },

    handleFilterByIndex(companiesReceived) {
      this.resultsArray = companiesReceived;
      this.showSearchResultsTable = true;
    },
    toggleIndexTabs(stockIndex) {
      this.$refs.indexTabs.filterByIndex(stockIndex);
    },
    toggleSearchBar() {
      this.searchBarToggled = true;
      const height = this.$refs.searchBarAndIndexTabContainer.clientHeight;
      window.scrollBy(0, -height);
      this.hiddenSearchBarHeight = height;
      this.scrollEmittedByToggleSearchBar = true;
      this.taxoSearchBarName = "eu_taxonomy_search_bar_scrolled";
    },
  },
};
</script>
