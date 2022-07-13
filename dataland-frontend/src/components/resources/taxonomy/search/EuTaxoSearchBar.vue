<template>
  <MarginWrapper>
    <div class="grid">
      <div class="col-8 text-left">
        <span class="p-fluid">
          <span class="p-input-icon-left p-input-icon-right">
            <i class="pi pi-search" aria-hidden="true" style="z-index: 20; color: #958d7c" />
            <i v-if="loading" class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
            <i v-else aria-hidden="true" />
            <AutoComplete
              :suggestions="autocompleteArrayDisplayed"
              :name="taxoSearchBarName"
              :modelValue="modelValue"
              ref="autocomplete"
              inputClass="h-3rem"
              field="companyName"
              style="z-index: 10"
              placeholder="Search company by name or PermID"
              @input="handleInput"
              @complete="searchCompanyName"
              @keyup.enter="handleCompanyQuery"
              @item-select="handleItemSelect"
            >
              <template #footer>
                <ul
                  class="p-autocomplete-items pt-0"
                  v-if="autocompleteArray && autocompleteArray.length >= maxNumAutoCompleteEntries"
                >
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
import { ApiClientProvider } from "@/services/ApiClients";
import AutoComplete from "primevue/autocomplete";
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import { searchTaxonomyPageResponseMapper } from "@/utils/SearchTaxonomyPageResponseMapper";

export default {
  name: "EuTaxoSearchBar",
  components: { AutoComplete, MarginWrapper },

  emits: ["companies-received", "update:modelValue", "rendered"],

  props: {
    taxoSearchBarName: {
      type: String,
      default: "eu_taxonomy_search_bar_standard",
    },
    modelValue: {
      type: String,
      default: null,
    },
    maxNumAutoCompleteEntries: {
      type: Number,
      default: 3,
    },
  },

  mounted() {
    this.$emit("rendered", true);
    this.$refs.autocomplete.focus();
  },

  data() {
    return {
      autocompleteArray: [],
      autocompleteArrayDisplayed: null,
      loading: false,
    };
  },

  inject: ["getKeycloakInitPromise", "keycloak_init"],
  methods: {
    handleInput(event) {
      this.$emit("update:modelValue", event.target.value);
    },

    handleItemSelect(event) {
      this.$router.push(`/companies/${event.value.companyId}/eutaxonomies`);
    },

    handleCompanyQuery(event) {
      this.queryCompany(event.target.value);
      this.$refs.autocomplete.hideOverlay();
    },

    async queryCompany(companyName) {
      try {
        this.loading = true;
        const companyDataControllerApi = await new ApiClientProvider(
          this.getKeycloakInitPromise(),
          this.keycloak_init
        ).getCompanyDataControllerApi();
        const response = await companyDataControllerApi.getCompanies(companyName, "", false);
        this.mappedResponse = searchTaxonomyPageResponseMapper(response.data);
      } catch (error) {
        console.error(error);
      } finally {
        this.loading = false;
        this.selectedIndex = null;
        this.$emit("companies-received", this.mappedResponse);
      }
    },

    async searchCompanyName(companyName) {
      try {
        this.loading = true;
        const companyDataControllerApi = await new ApiClientProvider(
          this.getKeycloakInitPromise(),
          this.keycloak_init
        ).getCompanyDataControllerApi();
        const response = await companyDataControllerApi.getCompanies(companyName.query, "", true);
        this.autocompleteArray = searchTaxonomyPageResponseMapper(response.data);
        this.autocompleteArrayDisplayed = this.autocompleteArray.slice(0, this.maxNumAutoCompleteEntries);
      } catch (error) {
        console.error(error);
      } finally {
        this.loading = false;
        this.selectedIndex = null;
      }
    },
  },

  unmounted() {
    window.removeEventListener("scroll", this.handleScroll);
  },
};
</script>
