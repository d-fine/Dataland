<template>
  <TabMenu :model="Object.keys(stockIndexObject)
        .map(e => {
          return {
            label: stockIndexObject[e],
            command: () => {
              handleIndexTabClick(e, Object.keys(stockIndexObject).indexOf(e))
            }
          }
        })"  v-model:activeIndex="activeIndex" >
  </TabMenu>
</template>

<script>

import TabMenu from 'primevue/tabmenu';
import {StringHumanizer} from "@/utils/StringHumanizer"
const stringHumanizer = new StringHumanizer()
import apiSpecs from "@/../build/clients/backend/backendOpenApi.json";
const stockIndexObject = apiSpecs.components.schemas.CompanyInformation.properties["indices"]
    .items.enum.reduce((a, v) => ({ ...a, [v]: stringHumanizer.humanize(v)}), {})
export default {
  name: "IndexTabs",
  components: {TabMenu},
  props: {
  stockIndexObject: {
    type: Object,
    default: stockIndexObject
  },
    initIndex:{
     type: Number
    }
  },
  data(){
    return {
      activeIndex: null,
    }
  },
  methods: {
    handleIndexTabClick(element, currentIndex){
      this.$emit("tab-click", currentIndex, element)
    },
    change(index){
      this.activeIndex = index
    }
  },
  mounted() {
      this.activeIndex = this.initIndex
  }

}
</script>

<style scoped>

</style>