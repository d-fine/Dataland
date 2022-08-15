<template>
  <TabMenu class="d-indextabs" :model="model" v-model:activeIndex="activeIndex"></TabMenu>
</template>

<style>
.d-indextabs .p-tabmenu-nav {
  position: relative;
  border: none !important;
}

.d-indextabs .p-menuitem-link {
  border: none !important;
}

.d-indextabs .p-tabmenu-ink-bar {
  display: block !important;
  position: absolute !important;
  bottom: 0;
  height: 4px;
}

.d-indextabs .p-tabmenu-ink-bar::after {
  display: block;
  margin-left: auto;
  margin-right: auto;
  content: "";
  width: 80%;
  height: 100%;
  background: #e67f3f;
}

.d-indextabs.p-tabmenu {
  overflow-y: hidden !important;
}
</style>

<script>
import TabMenu from "primevue/tabmenu";
import { humanizeString } from "@/utils/StringHumanizer";
import apiSpecs from "../../../../build/clients/backend/backendOpenApi.json";
import { getCompanyDataForFrameworkDataSearchPage } from "@/utils/SearchCompaniesForFrameworkDataPageDataRequester";

const stockIndices = apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum;

export default {
  name: "IndexTabMenu",
  components: { TabMenu },
  emits: ["tab-click", "companies-received"],
  props: {
    initIndex: {
      type: Number,
    },
  },

  inject: ["getKeycloakPromise"],

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
      const resultsArray = await getCompanyDataForFrameworkDataSearchPage(
        "",
        stockIndex,
        false,
        [],
        this.getKeycloakPromise()
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
