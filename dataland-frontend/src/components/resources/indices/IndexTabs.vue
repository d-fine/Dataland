<template>
  <TabMenu :model="model" v-model:activeIndex="activeIndex"></TabMenu>
</template>

<script>
import TabMenu from "primevue/tabmenu";
import { humanize } from "@/utils/StringHumanizer";
import apiSpecs from "../../../../build/clients/backend/backendOpenApi.json";
import { ApiClientProvider } from "@/services/ApiClients";
import { searchTaxonomyPageResponseMapper } from "@/utils/SearchTaxonomyPageResponseMapper";

const stockIndices = apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum;

export default {
  name: "IndexTabs",
  components: { TabMenu },
  emits: ["tab-click", "companies-received"],
  props: {
    initIndex: {
      type: Number,
    },
  },

  inject: ["getKeycloakInitPromise", "keycloak_init"],

  computed: {
    model() {
      return stockIndices.map((stockIndex, index) => {
        return {
          label: humanize(stockIndex),
          command: () => {
            this.$emit("tab-click", stockIndex, index);
          },
        };
      });
    },
  },

  methods: {
    async filterByIndex(stockIndex) {
      try {
        const companyDataControllerApi = await new ApiClientProvider(
          this.getKeycloakInitPromise(),
          this.keycloak_init
        ).getCompanyDataControllerApi();
        const response = await companyDataControllerApi.getCompanies("", stockIndex, false);
        this.mappedResponse = searchTaxonomyPageResponseMapper(response.data);
      } catch (error) {
        console.error(error);
      } finally {
        this.$emit("companies-received", this.mappedResponse);
      }
    },
  },

  data() {
    return {
      activeIndex: null,
    };
  },
  mounted() {
    this.activeIndex = this.initIndex;
  },
};
</script>
