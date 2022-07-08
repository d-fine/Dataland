<template>
  <MarginWrapper >
    <div class="grid">
      <div class="col-8 text-left">
        <span class="p-fluid">
          <span class="p-input-icon-left p-input-icon-right ">
            <i class="pi pi-search" aria-hidden="true" style="z-index:20; color:#958D7C"/>
            <i v-if="loading" class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index:20; color:#e67f3f"/>
            <i v-else aria-hidden="true"/>
            <AutoComplete
                :suggestions="autocompleteArrayDisplayed" :name="taxoSearchBarName"
                :modelValue="modelValue"
                @input="handleInput"
                ref="autocomplete" inputClass="h-3rem" field="companyName" style="z-index:10"
                placeholder="Search company by name or PermID"
                @complete="searchCompanyName"
                @keyup.enter="handleCompanyQuery" @item-select="handleItemSelect"
                >
              <template #footer>
                <ul class="p-autocomplete-items pt-0" v-if="autocompleteArray && autocompleteArray.length >= maxNumAutoCompleteEntries">
                  <li class="p-autocomplete-item text-primary font-semibold" @click="handleCompanyQuery">
                    View all results.
                  </li>
                </ul>
              </template>
            </AutoComplete>
          </span>
        </span>
      </div>
    </div>
  </MarginWrapper>
</template>

<script>
import {ApiClientProvider} from "@/services/ApiClients"
import AutoComplete from 'primevue/autocomplete';
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import {useRoute} from "vue-router"


export default {
  name: "EuTaxoSearchBar",
  components: {AutoComplete, MarginWrapper},

  emits: ['queryCompany', 'filterByIndex', 'update:modelValue'],

  props: {
    taxoSearchBarName: {
      type: String,
      default: "eu_taxonomy_search_bar_standard"
    },
    modelValue: {
      type: String,
      default: null
    },
    maxNumAutoCompleteEntries: {
      type: Number,
      default: 3,
    }
  },

  mounted() {
    this.$refs.autocomplete.focus()
  },

  data() {
    return {
      route: useRoute(),
      autocompleteArray: [],
      autocompleteArrayDisplayed: null,
      loading: false
    }
  },

  inject: ['getKeycloakInitPromise','keycloak_init'],
  methods: {

    closeDropdown() {
      this.$refs.autocomplete.hideOverlay()
    },

    handleInput(event) {
      this.$emit('update:modelValue', event.target.value)
    },

    handleItemSelect(event) {
      this.$router.push(`/companies/${event.value.companyId}/eutaxonomies`)
    },

    handleCompanyQuery(event) {
      this.$router.push({name: 'Search Eu Taxonomy', query: {input: event.target.value}})
      this.queryCompany(event.target.value)
      this.closeDropdown()
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
        this.loading = true
        const companyDataControllerApi = await new ApiClientProvider(this.getKeycloakInitPromise(), this.keycloak_init).getCompanyDataControllerApi()
        this.responseArray = await companyDataControllerApi.getCompanies("", stockIndex, false).then(this.responseMapper)
      } catch (error) {
        console.error(error)
      } finally {
        this.loading = false
        this.$emit("filterByIndex", this.responseArray)
      }
    },

    async queryCompany(companyName) {
      try {
        this.loading = true
        const companyDataControllerApi = await new ApiClientProvider(this.getKeycloakInitPromise(), this.keycloak_init).getCompanyDataControllerApi()
        this.responseArray = await companyDataControllerApi.getCompanies(companyName, "", false).then(this.responseMapper)
        this.filteredCompaniesBasic = this.responseArray.slice(0, 3)
      } catch (error) {
        console.error(error)
      } finally {
        this.loading = false
        this.showSearchResultsTable = true
        this.selectedIndex = null
        this.$emit("queryCompany", this.responseArray)
      }
    },

    async searchCompanyName(companyName) {
      try {
        this.loading = true
        const companyDataControllerApi = await new ApiClientProvider(this.getKeycloakInitPromise(), this.keycloak_init).getCompanyDataControllerApi()
        this.autocompleteArray = await companyDataControllerApi.getCompanies(companyName.query, "", true).then(this.responseMapper)
        this.autocompleteArrayDisplayed = this.autocompleteArray.slice(0, this.maxNumAutoCompleteEntries)
      } catch (error) {
        console.error(error)
      } finally {
        this.loading = false
        this.selectedIndex = null
      }
    }
  },

  unmounted() {
    window.removeEventListener('scroll', this.handleScroll);
  }
}
</script>
