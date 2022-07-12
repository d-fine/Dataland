<template>
  <TabMenu :model="model" v-model:activeIndex="activeIndex"> </TabMenu>
</template>

<script>
import TabMenu from "primevue/tabmenu";
import { humanize } from "@/utils/StringHumanizer";
import apiSpecs from "../../../../build/clients/backend/backendOpenApi.json";
import { ApiClientProvider } from "@/services/ApiClients";
const stockIndices = apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum;

export default {
  name: "IndexTabs",
  components: { TabMenu },
  emits: ["tab-click", "filterByIndex"],
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
    responseMapper(response) {
      return response.data.map((e) => ({
        companyName: e.companyInformation.companyName,
        companyInformation: e.companyInformation,
        companyId: e.companyId,
        permId: e.companyInformation.identifiers
          .map((identifier) => {
            return identifier.identifierType === "PermId" ? identifier.identifierValue : "";
          })
          .pop(),
      }));
    },

    async filterByIndex(stockIndex) {
      try {
        const companyDataControllerApi = await new ApiClientProvider(
          this.getKeycloakInitPromise(),
          this.keycloak_init
        ).getCompanyDataControllerApi();
        this.responseArray = await companyDataControllerApi
          .getCompanies("", stockIndex, false)
          .then(this.responseMapper);
      } catch (error) {
        console.error(error);
      } finally {
        this.$emit("filterByIndex", this.responseArray);
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
