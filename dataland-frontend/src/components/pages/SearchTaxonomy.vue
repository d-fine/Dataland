<template>
  <AuthenticationWrapper>
    <TheHeader/>
    <TheContent>
      <SearchTaxonomyHeader :scrolled="pageScrolled"/>
      <EuTaxoSearchBar v-model="currentInput"
                       v-if="!pageScrolled"
                       ref="euTaxoSearchBar"
                       @companyToQuery="handleCompanyQuery"/>
      <MarginWrapper>
        <IndexTabs v-if="!pageScrolled"
                   ref="indexTabs"
                   :initIndex="selectedIndex"
                   @tab-click="toggleIndexTabs"/>
      </MarginWrapper>
      <div class="col-12 align-items-center grid bg-white d-search-toggle fixed" v-if="pageScrolled" >
          <EuTaxoSearchBar class="col-12"
                           v-model="currentInput"
                           v-if="searchBarActivated"
                           ref="euTaxoSearchBar"
                           taxo-search-bar-name="eu_taxonomy_search_bar_scrolled"
                           @companyToQuery="handleCompanyQuery"
          />
        <span class="mr-3 font-semibold" v-if="!searchBarActivated">Search EU Taxonomy data</span>
        <Button v-if="!searchBarActivated"
                name="search_bar_collapse"
                icon="pi pi-search" class="p-button-rounded surface-ground border-none m-2"
                @click="toggleSearchBar">
          <i class="pi pi-search" aria-hidden="true" style="z-index:20; color:#958D7C"/>
        </Button>
        <IndexTabs ref="indexTabs"
                   :initIndex="selectedIndex"
                   @tab-click="toggleIndexTabs" />
      </div>
      <EuTaxoSearchResults v-if="showSearchResultsTable" :data="responseArray"/>
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
import {ApiClientProvider} from "@/services/ApiClients"
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

  created() {
    window.addEventListener('scroll', this.handleScroll)
  },
  mounted() {
    if (this.route.query && this.route.query.input) {
      this.currentInput = this.route.query.input
      this.queryCompany()
    } else if (this.route.path === "/searchtaxonomy") {
      this.filterByIndex(stockIndices[this.selectedIndex])
    }
  },

  data() {
    return {
      searchBarActivated: false,
      pageScrolled: false,
      route: useRoute(),
      selectedIndex: 1,
      showSearchResultsTable: false,
      responseArray: [],
      latestScrollPosition: 0,
      currentInput: "",
    }
  },

  inject: ['getKeycloakInitPromise','keycloak_init'],

  watch: {
    pageScrolled(value) {
      if (!value) {
        this.searchBarActivated = false
      }
    }
  },

  methods: {



    handleScroll() {
      const windowScrollY = window.scrollY
      if(this.latestScrollPosition > windowScrollY){
        //ScrollUP event
        this.latestScrollPosition = windowScrollY
        this.pageScrolled = document.documentElement.scrollTop >= 50
      } else{
        //ScrollDOWN event
        this.pageScrolled = document.documentElement.scrollTop > 80
        this.latestScrollPosition = windowScrollY
      }
    },

    handleCompanyQuery(value) {
      this.$router.push({name: 'Search Eu Taxonomy', query: {input: value}})
      this.queryCompany()
      this.$refs.indexTabs.activeIndex = null
      this.showSearchResultsTable = true
    },

    toggleIndexTabs(stockIndex, index) {
      this.selectedIndex = index
      this.filterByIndex(stockIndex)
    },

    toggleSearchBar() {
      this.searchBarActivated = !this.searchBarActivated
    },

    responseMapper(response) {
      return response.data.map(e => ({
        "companyName": e.companyInformation.companyName,
        "companyInformation": e.companyInformation,
        "companyId": e.companyId,
        "permId": e.companyInformation.identifiers.map((identifier) => {
          return identifier.identifierType === "PermId" ? identifier.identifierValue : ""
        }).pop()
      }))
    },

    async filterByIndex(stockIndex) {
      try {
        this.$refs.euTaxoSearchBar.loading = true
        const companyDataControllerApi = await new ApiClientProvider(this.getKeycloakInitPromise(), this.keycloak_init).getCompanyDataControllerApi()
        this.responseArray = await companyDataControllerApi.getCompanies("", stockIndex, false).then(this.responseMapper)
      } catch (error) {
        console.error(error)
      } finally {
        this.$refs.euTaxoSearchBar.loading = false
        this.showSearchResultsTable = true
      }
    },

    async queryCompany() {
      try {
        this.$refs.euTaxoSearchBar.loading  = true
        const companyDataControllerApi = await new ApiClientProvider(this.getKeycloakInitPromise(), this.keycloak_init).getCompanyDataControllerApi()
        this.responseArray = await companyDataControllerApi.getCompanies(this.currentInput, "", false).then(this.responseMapper)
        this.filteredCompaniesBasic = this.responseArray.slice(0, 3)
      } catch (error) {
        console.error(error)
      } finally {
        this.$refs.euTaxoSearchBar.loading  = false
        this.showSearchResultsTable = true
        this.selectedIndex = null
      }
    },
  },
}
</script>