<template>
  <TabMenu :model="model" v-model:activeIndex="activeIndex" >
  </TabMenu>
</template>

<script>

import TabMenu from 'primevue/tabmenu';
import {humanize} from "@/utils/StringHumanizer"
import apiSpecs from "../../../../build/clients/backend/backendOpenApi.json";
const stockIndices = apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum

export default {
  name: "IndexTabs",
  components: {TabMenu},
  emits: ['tab-click'],
  props: {
    initIndex:{
     type: Number
    }
  },
  computed: {
      model() {
        return stockIndices.map((stockIndex, index) => {
          return {
            label: humanize(stockIndex),
            command: () => {
              this.$emit("tab-click", stockIndex, index)
            }
          }
        })
      }
  },


  data(){
    return {
      activeIndex: null,
    }
  },
  mounted() {
      this.activeIndex = this.initIndex
  }

}
</script>