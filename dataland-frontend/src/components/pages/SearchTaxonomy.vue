<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent>
      <SearchTaxonomyHeader :scrolled="pageScrolled"/>
      <EuTaxoSearchBar v-model="currentInput"
                       ref="euTaxoSearchBarTop"
                       @queryCompany="handleCompanyQuery"
                       @rendered="handleEuTaxoSearchBarRender"/>
      <MarginWrapper>
        <IndexTabs v-if="!pageScrolled"
                   ref="indexTabs"
                   :initIndex="selectedIndex"
                   @tab-click="toggleIndexTabs"
                   @filterByIndex="handleFilterByIndex"/>
      </MarginWrapper>
      <div class="col-12 align-items-center grid bg-white d-search-toggle fixed" v-if="pageScrolled">
        <EuTaxoSearchBar class="col-12"
                         v-model="currentInput"
                         v-if="searchBarActivated"
                         taxo-search-bar-name="eu_taxonomy_search_bar_scrolled"
                         @queryCompany="handleCompanyQuery"/>
        <span class="mr-3 font-semibold" v-if="!searchBarActivated">Search EU Taxonomy data</span>
        <Button v-if="!searchBarActivated"
                name="search_bar_collapse"
                icon="pi pi-search" class="p-button-rounded surface-ground border-none m-2"
                @click="toggleSearchBar">
          <i class="pi pi-search" aria-hidden="true" style="z-index:20; color:#958D7C"/>
        </Button>
        <IndexTabs ref="indexTabs"
                   :initIndex="selectedIndex"
                   @tab-click="toggleIndexTabs"
                   @filterByIndex="handleFilterByIndex"/>
      </div>
      <EuTaxoSearchResults v-if="showSearchResultsTable" :data="resultsArray"/>
    </TheContent>
  </AuthenticationWrapper>
</template>
<script>

import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper"
import TheHeader from "@/components/structure/TheHeader"
import TheContent from "@/components/structure/TheContent"
import SearchTaxonomyHeader from "@/components/resources/taxonomy/search/SearchTaxonomyHeader"
import EuTaxoSearchBar from "@/components/resources/taxonomy/search/EuTaxoSearchBar"
import MarginWrapper from "@/components/wrapper/MarginWrapper"
import IndexTabs from "@/components/resources/indices/IndexTabs"
import Button from "primevue/button"
import EuTaxoSearchResults from "@/components/resources/taxonomy/search/EuTaxoSearchResults"
import {useRoute} from "vue-router"
import apiSpecs from "../../../build/clients/backend/backendOpenApi.json";

const stockIndices = apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum

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
    EuTaxoSearchResults
  },

  emits: ['queryCompany', 'update:modelValue'],

  created() {
    window.addEventListener('scroll', this.handleScroll)
  },

  data() {
    return {
      searchBarActivated: false,
      pageScrolled: false,
      route: useRoute(),
      selectedIndex: 1,
      showSearchResultsTable: false,
      resultsArray: [],
      latestScrollPosition: 0,
      currentInput: null,
    }
  },

  inject: ['getKeycloakInitPromise', 'keycloak_init', 'authenticated'],

  watch: {
    pageScrolled(value) {
      if (value) {
        this.$refs.euTaxoSearchBarTop.$refs.autocomplete.hideOverlay()
      }
      if (!value) {
        this.searchBarActivated = false
        this.$refs.euTaxoSearchBarTop.$refs.autocomplete.focus()
      }
    },
  },

  methods: {


    handleScroll() {
      const windowScrollY = window.scrollY
      if (this.latestScrollPosition > windowScrollY) {
        //ScrollUP event
        this.latestScrollPosition = windowScrollY
        this.pageScrolled = document.documentElement.scrollTop >= 50
        this.pageTopReached = document.documentElement.scrollTop === 0
      } else {
        //ScrollDOWN event
        this.pageScrolled = document.documentElement.scrollTop > 80
        this.latestScrollPosition = windowScrollY
      }
    },

    handleEuTaxoSearchBarRender() {
      if (this.route.query && this.route.query.input) {
        this.currentInput = this.route.query.input
        this.$refs.euTaxoSearchBar.queryCompany(this.currentInput)
      } else if (this.route.path === "/searchtaxonomy") {
        this.toggleIndexTabs(stockIndices[this.selectedIndex], this.selectedIndex)
      }
    }
    ,

      handleCompanyQuery(event)
      {
        this.selectedIndex = null
        this.$refs.indexTabs.activeIndex = null
        this.resultsArray = event
        this.showSearchResultsTable = true
        this.$router.push({name: 'Search Eu Taxonomy', query: {input: this.currentInput}})

      }
    ,

      handleFilterByIndex(event)
      {
        this.resultsArray = event
        this.showSearchResultsTable = true
      }
    ,

      toggleIndexTabs(stockIndex, index)
      {
        this.selectedIndex = index
        this.$refs.indexTabs.filterByIndex(stockIndex)
      }
    ,

      toggleSearchBar()
      {
        this.searchBarActivated = !this.searchBarActivated
      }
    ,
    },
  }
</script>
