<template>
  <TabMenu :model="model" v-model:activeIndex="activeIndex" >
  </TabMenu>
</template>

<script>

import TabMenu from 'primevue/tabmenu';
import {humanize} from "@/utils/StringHumanizer"
import apiSpecs from "@/../build/clients/backend/backendOpenApi.json";
export default {
  name: "IndexTabs",
  components: {TabMenu},
  emits: ['tab-click'],
  props: {
    indices: {
      type: Array,
      default (){
        return apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum
      }
    },
    initIndex:{
     type: Number
    }
  },
  computed: {
      model() {
        return this.indices.map((e, index) => {
          return {
            label: this.humanize(e),
            command: () => {
             this.handleIndexTabClick(e, index)
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
  methods: {
    handleIndexTabClick(element, currentIndex){
      this.$emit("tab-click", currentIndex, element)
    },
    change(index){
      this.activeIndex = index
    },
    humanize(text) {
      return humanize(text)
    }
  },
  mounted() {
      this.activeIndex = this.initIndex
  }

}
</script>