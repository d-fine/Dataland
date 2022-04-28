<template>
  <Card class="bg-white d-card mr-2 cursor-pointer" >
    <template #stockIndex></template>
    <template #content>
      <div class="grid align-items-center">
        <div class="col-12 text-left pb-0">
        <strong>{{ indexName }}</strong>
        </div>
      </div>
    </template>
  </Card>
</template>

<script>
import Card from "primevue/card";
import {humanize} from "@/utils/StringHumanizer"

export default {
  name: "IndexCard",
  components: {Card},
  computed: {
    indexName(){
      return humanize(this.stockIndices[this.index])
    }
  },
  props: {
    stockIndices: {
      type: Array
    },
    index: {
      type: Number
    },
  }

}
</script>