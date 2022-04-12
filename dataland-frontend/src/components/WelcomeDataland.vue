<template>
  <div class="grid">
    <div class="col-12 surface-ground">
      <div class="grid align-items-center">
        <div class="col-4 col-offset-2">
          <h1 class="text-justify">COME TOGETHER TO CREATE A DATASET THAT NOBODY CAN CREATE ALONE WHILE SHARING THE COSTS.</h1>
        </div>
        <div class="col-2 col-offset-2">
          <img alt="Dataland logo" src="@/assets/images/logos/bg_graphic_vision.svg" class="mx-auto">
        </div>
      </div>
    </div>
    <div class="col">
      <Button v-for="item in items" :key="item" @click="currentItem = item">{{item}}</Button>
      <component :is="currentItem"></component>
      <TabMenu :model="items" :label="items">
        <template #item="{item}">
          <Button @click="currentItem = item">{{item}}</Button>
        </template>
      </TabMenu>
    </div>
  </div>
</template>

<script>
import SearchTaxonomy from "@/components/pages/taxonomy/SearchTaxonomy.vue";
import IndexPanel from "@/components/pages/indices/IndexPanel.vue";
import Button from "primevue/button";
import TabMenu from "primevue/tabmenu";
export default {
  name: 'WelcomeDataland',
  components: {SearchTaxonomy, IndexPanel, Button, TabMenu},
  data(){
    return {
      items:["SearchTaxonomy", "IndexPanel"],
      currentItem: "SearchTaxonomy"
    }
  }
}
</script>


