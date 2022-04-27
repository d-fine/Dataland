<template>
  <MarginWrapper >
    <div class="grid">
      <div class="col-8 text-left" v-if="!scrolled">
        <span class="p-fluid">
          <span class="p-input-icon-left p-input-icon-right ">
            <i class="pi pi-search" aria-hidden="true" style="z-index:20; color:#958D7C"/>
            <i v-if="loading" class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index:20; color:#e67f3f"/>
            <i v-else aria-hidden="true"/>
            <AutoComplete
                v-model="selectedCompany" :suggestions="filteredCompaniesBasic" @focus="focused" @focusout="unfocused"
                @complete="searchCompany($event)" placeholder="Search a company by name, ISIN, PermID or LEI" inputClass="h-3rem" ref="autocomplete"
                field="companyName" style="z-index:10" name="eu_taxonomy_search_input"
                @keyup.enter="handleQuery" @item-select="handleItemSelect">
              <template #footer>
                <ul v-if="autocompleteArray && autocompleteArray.length > 0" class="p-autocomplete-items pt-0">
                  <li class="p-autocomplete-item text-primary font-semibold" @click="handleQuery">
                    View all results.
                  </li>
                </ul>
              </template>
            </AutoComplete>
          </span>
        </span>
      </div>
      <div class="col-12 align-items-center grid bg-white d-search-toggle fixed" v-if="scrolled">
        <span class="mr-3 font-semibold">Search EU Taxonomy data</span>
        <Button class="p-button-rounded surface-ground border-none" @click="activateSearchBar" name="search_bar_collapse">
          <i class="pi pi-search" aria-hidden="true" style="z-index:20; color:#958D7C"/>
        </Button>
        <IndexTabs v-if="showIndexTabs" :initIndex="index" @tab-click="toggleIndexTabs" ref="indexTabs"/>
      </div>
    </div>
  </MarginWrapper>
  <MarginWrapper>
    <IndexTabs v-if="showIndexTabs && !scrolled" :initIndex="index"  @tab-click="toggleIndexTabs" ref="indexTabs"/>
  </MarginWrapper>
  <EuTaxoSearchResults v-if="collection" :data="responseArray"/>
</template>

<script>
import {CompanyDataControllerApi} from "../../../../../build/clients/backend/api";
import {ApiWrapper} from "@/services/ApiWrapper"
import AutoComplete from 'primevue/autocomplete';
import EuTaxoSearchResults from "@/components/resources/taxonomy/search/EuTaxoSearchResults";
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import IndexTabs from "@/components/resources/indices/IndexTabs";
import Button from "primevue/button";
import {useRoute} from "vue-router"

const companyDataControllerApi = new CompanyDataControllerApi()
const getCompaniesWrapper = new ApiWrapper(companyDataControllerApi.getCompanies)


export default {
  name: "EuTaxoSearchBar",
  components: {MarginWrapper, EuTaxoSearchResults, AutoComplete, Button, IndexTabs},
  props: {
    paramsSelection: {
      type: String,
      default: ""
    },
    stockIndices: {
      type: Array,
    },
  },
  data() {
    return {
      route: useRoute(),
      showIndexTabs: false,
      index: null,
      scrolled: false,
      focus: false,
      collection: false,
      responseArray: [],
      autocompleteArray: [],
      loading: false,
      selectedCompany: null,
      filteredCompaniesBasic: null
    }
  },
  created() {
    window.addEventListener('scroll', this.handleScroll);
  },
  mounted() {
    if (this.route.query && this.route.query.input) {
      this.selectedCompany = this.route.query.input
      this.queryCompany()
    }
  },
  methods: {
    activateSearchBar() {
      window.addEventListener('scroll', () => {
        if (document.body.scrollTop < 50 || document.documentElement.scrollTop < 50) {
          this.$refs.autocomplete.focus()
        }
      });
      window.scrollTo({top: 0, behavior: 'smooth'})

    },
    close() {
      this.$refs.autocomplete.hideOverlay()
    },
    focused() {
      this.$emit('autocomplete-focus', true)
      if (this.$refs.indexTabs) {
        this.$refs.indexTabs.activeIndex = null
      }
    },
    handleItemSelect() {
      this.collection = false;
      this.$router.push(`/companies/${this.selectedCompany.companyId}/eutaxonomies`)
    },
    handleQuery() {
      this.collection = true;
      this.$router.push({name: 'Search Eu Taxonomy', query: {input: this.selectedCompany}});
      this.queryCompany();
      this.close();
    },
    handleScroll() {
      this.scrolled = document.body.scrollTop > 150 || document.documentElement.scrollTop > 150;
      this.$emit('scrolling', this.scrolled)
    },

    responseMapper(response) {
      return response.data.map(e => ({
        "companyName": e.companyInformation.companyName,
        "companyInformation": e.companyInformation,
        "companyId": e.companyId
      }))
    },
    toggleIndexTabs(stockIndex, index) {
      this.index = index
      this.showIndexTabs = true
      this.filterByIndex(stockIndex)
    },
    unfocused() {
      this.$emit('autocomplete-focus', false)
    },
    async filterByIndex(stockIndex) {
      try {
        this.loading = true
        this.responseArray = await getCompaniesWrapper.perform("", stockIndex, false).then(this.responseMapper)
        this.filteredCompaniesBasic = this.responseArray.slice(0, 3)
      } catch (error) {
        console.error(error)
      } finally {
        this.loading = false
        this.collection = true
      }
    },
    async queryCompany() {
      try {
        this.loading = true
        this.showIndexTabs = true
        this.responseArray = await getCompaniesWrapper.perform(this.selectedCompany, "", false).then(this.responseMapper)
        this.filteredCompaniesBasic = this.responseArray.slice(0, 3)
      } catch (error) {
        console.error(error)
      } finally {
        this.loading = false
        this.collection = true
        this.index = null
      }
    },
    async searchCompany(event) {
      try {
        this.loading = true
        this.autocompleteArray = await getCompaniesWrapper.perform(event.query, "", true).then(this.responseMapper)
        this.filteredCompaniesBasic = this.autocompleteArray.slice(0, 3)
      } catch (error) {
        console.error(error)
      } finally {
        this.loading = false
        this.index = null
      }
    }
  },
  emits: ['autocomplete-focus', 'scrolling'],
  unmounted() {
    window.removeEventListener('scroll', this.handleScroll);
  }
}
</script>