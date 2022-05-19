<template>
  <Card class="bg-white d-infocard d-card mr-2" >
    <template #title></template>
    <template #content>
      <div class="grid ">
        <div class="col-12 text-left">
          <strong>{{ title }}</strong>
        </div>
      </div>
        <div class="col-12 text-left">
          <span>{{value}}</span><i class="col-offset-10 material-icons" aria-hidden="true" v-tooltip.top="tooltipText ? {
                value: tooltipText,
                class: 'd-tooltip-mw25'
              } : '' ">info
        </i>
        </div>
    </template>
  </Card>
</template>

<script>

import Card from "primevue/card";
import Tooltip from 'primevue/tooltip';

export default {
  name: "TaxoInfoCard",
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
      default: ""
    },
  },
}
</script>