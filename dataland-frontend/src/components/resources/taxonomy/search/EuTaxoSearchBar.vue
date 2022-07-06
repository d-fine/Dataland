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
                v-model="selectedCompany" :suggestions="autocompleteArrayDisplayed" :name="taxoSearchBarName"
                ref="autocomplete" inputClass="h-3rem" field="companyName" style="z-index:10"
                placeholder="Search company by name or PermID"
                @focus="focused" @focusout="unfocused"
                @complete="searchCompany"
                @keyup.enter="handleQuery" @item-select="handleItemSelect"
                >
              <template #footer>
                <ul class="p-autocomplete-items pt-0" v-if="autocompleteArray && autocompleteArray.length > 0">
                  <li class="p-autocomplete-item text-primary font-semibold" @click="handleQuery">
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
  props: {
    taxoSearchBarName: {
      type: String,
      default: "eu_taxonomy_search_bar_standard"
    },
  },

  mounted() {
    this.$refs.autocomplete.focus()
  },

  data() {
    return {
      route: useRoute(),
      autocompleteArray: [],
      autocompleteArrayDisplayed: null,
      loading: false,
      selectedCompany: null,
    }
  },

  inject: ['getKeycloakInitPromise','keycloak_init'],
  methods: {

    closeDropdown() {
      this.$refs.autocomplete.hideOverlay()
    },

    focused() {
      this.$emit('autocomplete-focus', true)
    },
    unfocused() {
      this.$emit('autocomplete-focus', false)
    },


    handleItemSelect() {
      this.collection = false;
      this.$router.push(`/companies/${this.selectedCompany.companyId}/eutaxonomies`)
    },

    handleQuery(event) {
      this.$emit("companyToQuery", event.target.value)
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

    async searchCompany(event) {
      try {
        this.loading = true
        const companyDataControllerApi = await new ApiClientProvider(this.getKeycloakInitPromise(), this.keycloak_init).getCompanyDataControllerApi()
        this.autocompleteArray = await companyDataControllerApi.getCompanies(event.query, "", true).then(this.responseMapper)
        this.autocompleteArrayDisplayed = this.autocompleteArray.slice(0, 3)
      } catch (error) {
        console.error(error)
      } finally {
        this.loading = false
        this.selectedIndex = null
      }
    }
  },


  emits: ['autocomplete-focus', 'companyToQuery'],

  unmounted() {
    window.removeEventListener('scroll', this.handleScroll);
  }
}
</script>
