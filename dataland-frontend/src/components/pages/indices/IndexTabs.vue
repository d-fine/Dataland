<template>
  <TabMenu :model="items" v-model:activeIndex="activeIndex"></TabMenu>
  <button @click="change(3)">Chang active tab</button>
</template>

<script>

import TabMenu from 'primevue/tabmenu';
export default {
  name: "IndexTabs",
  components: {TabMenu},
  props: ['indexArray', 'initIndex'],
  data(){
    return {
      activeIndex: null,
      items: this.indexArray.map(e => {
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