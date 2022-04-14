<template>
  <TabMenu :model="Object.keys(stockIndexObject).map(e => {
  return {
    label: stockIndexObject[e],
    command: (event) => {
      tab(event.item.label)
    }
  }})"

           v-model:activeIndex="activeIndex">

  </TabMenu>
</template>

<script>

import TabMenu from 'primevue/tabmenu';
import {stockIndexObject} from "@/utils/indexMapper";

export default {
  name: "IndexTabs",
  components: {TabMenu},
  props: ['initIndex'],
  data(){
    return {
      activeIndex: null,
      stockIndexObject: stockIndexObject(),
    }
  },
  methods: {
    tab(element){
      console.log("Tab function executed:")
      console.dir(element)
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