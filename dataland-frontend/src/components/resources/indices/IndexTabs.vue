<template>
  <TabMenu :model="model" v-model:activeIndex="activeIndex">

  </TabMenu>
</template>

<style>
.p-tabmenu-nav {
  position: relative;
  border: none !important;
}

.p-menuitem-link {
  border: none !important;
}

.p-tabmenu-ink-bar {
  display: block !important;
  position: absolute !important;
  bottom: 0;
  height: 4px;
}

.p-tabmenu-ink-bar::after {
  display: block;
  margin-left: auto;
  margin-right: auto;
  content: "";
  width: 80%;
  height: 100%;
  background: #e67f3f;
}
</style>

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

  inject: ["getKeycloakInitPromise", "keycloak_init"],

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
        this.keycloak_init
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
