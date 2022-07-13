<template>
  <AuthenticationWrapper>
    <TheHeader/>
    <TheContent>
      <div :class="[searchBarToggled && pageScrolled ? ['d-search-toggle', 'fixed'] : '']">

        <SearchTaxonomyHeader/>
        <MarginWrapper>

          <EuTaxoSearchBar
              v-model="currentInput"
              ref="euTaxoSearchBar"
              @companies-received="handleCompanyQuery"
              @rendered="handleEuTaxoSearchBarRender">
          </EuTaxoSearchBar>

          <div
              :class="[pageScrolled ? ['col-12', 'align-items-center', 'grid', 'bg-white', 'd-search-toggle', 'fixed'] : '']">

            <span class="mr-3 font-semibold" v-if="!searchBarToggled && pageScrolled">Search EU Taxonomy data</span>
            <Button
                v-if="!searchBarToggled && pageScrolled"
                name="search_bar_collapse"
                icon="pi pi-search"
                class="p-button-rounded surface-ground border-none m-2"
                @click="toggleSearchBar"
            >
              <i class="pi pi-search" aria-hidden="true" style="z-index: 20; color: #958d7c"/>
            </Button>
            <IndexTabs
                ref="indexTabs"
                :initIndex="selectedIndex"
                @tab-click="toggleIndexTabs"
                @companies-received="handleFilterByIndex">
            </IndexTabs>

          </div>

        </MarginWrapper>

      </div>

      <EuTaxoSearchResults v-if="showSearchResultsTable" :data="resultsArray"/>
    </TheContent>
  </AuthenticationWrapper>
</template>
<script>
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper";
import TheHeader from "@/components/structure/TheHeader";
import TheContent from "@/components/structure/TheContent";
import SearchTaxonomyHeader from "@/components/resources/taxonomy/search/SearchTaxonomyHeader";
import EuTaxoSearchBar from "@/components/resources/taxonomy/search/EuTaxoSearchBar";
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import IndexTabs from "@/components/resources/indices/IndexTabs";
import Button from "primevue/button";
import EuTaxoSearchResults from "@/components/resources/taxonomy/search/EuTaxoSearchResults";
import {useRoute} from "vue-router";
import apiSpecs from "../../../build/clients/backend/backendOpenApi.json";

const stockIndices = apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum;

export default {
  name: "SearchTaxonomy",
  components: {
    AuthenticationWrapper,
    TheHeader,
    TheContent,
    SearchTaxonomyHeader,
    EuTaxoSearchBar,
    MarginWrapper,
    IndexTabs,
    Button,
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
      selectedIndex: 1,
      showSearchResultsTable: false,
      resultsArray: [],
      latestScrollPosition: 0,
      currentInput: null,
    };
  },

  watch: {
    pageScrolled(pageScrolledNew) {
      if (pageScrolledNew) {
        this.$refs.euTaxoSearchBar.$refs.autocomplete.hideOverlay();
      }
      if (!pageScrolledNew) {
        this.searchBarToggled = false;
        this.$refs.euTaxoSearchBar.$refs.autocomplete.focus();
      }
    },
  },

  methods: {
    handleScroll() {
      const windowScrollY = window.scrollY;
      if (this.latestScrollPosition > windowScrollY) {
        //ScrollUP event
        this.latestScrollPosition = windowScrollY;
        this.pageScrolled = document.documentElement.scrollTop >= 50;
      } else {
        //ScrollDOWN event
        this.pageScrolled = document.documentElement.scrollTop > 80;
        this.latestScrollPosition = windowScrollY;
      }
    },

    handleEuTaxoSearchBarRender() {
      if (this.route.query && this.route.query.input) {
        this.currentInput = this.route.query.input;
        this.$refs.euTaxoSearchBar.queryCompany(this.currentInput);
      } else if (this.route.path === "/searchtaxonomy") {
        this.$refs.euTaxoSearchBar.$refs.autocomplete.focus()
        this.toggleIndexTabs(stockIndices[this.selectedIndex], this.selectedIndex);
      }
    },
    handleCompanyQuery(companiesReceived) {
      this.selectedIndex = null;
      this.$refs.indexTabs.activeIndex = null;
      this.resultsArray = companiesReceived;
      this.showSearchResultsTable = true;
      this.$router.push({name: "Search Eu Taxonomy", query: {input: this.currentInput}});
    },

    handleFilterByIndex(companiesReceived) {
      this.resultsArray = companiesReceived;
      this.showSearchResultsTable = true;
    },
    toggleIndexTabs(stockIndex, index) {
      this.selectedIndex = index;
      this.$refs.indexTabs.filterByIndex(stockIndex);
    },
    toggleSearchBar() {
      this.searchBarToggled = !this.searchBarToggled;
    },
  },
};
</script>
