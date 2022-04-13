<template>
  <TabMenu :model="items" v-model:activeIndex="activeIndex"></TabMenu>
</template>

<script>
const indexArray = [
  "CDAX",
  "DAX",
  "General Standards",
  "GEX",
  "MDAX",
  "Prime Standards",
  "SDAX",
  "TecDAX",
  "ScaleHDAX",
  "DAX 50 ESG"
]
import TabMenu from 'primevue/tabmenu';
export default {
  name: "IndexTabs",
  components: {TabMenu},
  data(){
    return {
      activeIndex: null,
      items: indexArray.map(e => {
        return {
          label: e,
          command: this.tab
        }
      })
    }
  },
  methods: {
    tab(){
      console.log("Tab function executed:")
      console.dir(this.activeIndex)
    }
  }
}
</script>

<style scoped>

</style>