<template>
  <Card class="bg-white d-infocard d-card mr-2" >
    <template #title></template>
    <template #content>
      <div class="grid " v-tooltip.top="{
              value: tooltipText,
              class: 'd-tooltip-mw25'
            }">
        <div class="col-12 text-left">
          <strong>{{ title }}</strong>
        </div>
        <div class="col-12 text-left">
          <span>{{value}}</span>
        </div>
      </div>
    </template>
  </Card>
</template>

<script>

import Card from "primevue/card";
import Tooltip from 'primevue/tooltip';

export default {
  name: "TaxoCard",
  components: {Card},
  directives: {
    'tooltip': Tooltip
  },
  props: {
    title: {
      type: String
    },
    value: {
      type: String
    },
    tooltipText: {
      type: String,
      default: "Information about to come"
    },
  },
}
</script>
<style>

.d-card > .p-card-body {
  padding-top: 0.5em;
  padding-bottom: 0.5em;
}

</style>