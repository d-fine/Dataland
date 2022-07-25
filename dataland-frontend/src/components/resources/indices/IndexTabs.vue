<template>
  <TabMenu :model="model" v-model:activeIndex="activeIndex"></TabMenu>
</template>

<script>
import TabMenu from "primevue/tabmenu";
import { humanizeString } from "@/utils/StringHumanizer";
import apiSpecs from "../../../../build/clients/backend/backendOpenApi.json";
import { getCompanyDataForTaxonomyPage } from "@/utils/SearchTaxonomyPageCompanyDataRequester";

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

  inject: ["getKeycloakInitPromise"],

  computed: {
    model() {
      return stockIndices.map((stockIndex, index) => {
        return {
          label: humanizeString(stockIndex),
          command: () => {
            this.$emit("tab-click", stockIndex, index);
          },
        };
      });
    },
  },

  methods: {
    async filterByIndex(stockIndex) {
      const resultsArray = await getCompanyDataForTaxonomyPage(
        "",
        stockIndex,
        false,
        this.getKeycloakInitPromise(),
      );
      this.$emit("companies-received", resultsArray);
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
