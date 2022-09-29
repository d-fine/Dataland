<template>
  <Card class="bg-white d-infocard d-card mr-2">
    <template #title></template>
    <template #content>
      <div class="grid">
        <div class="col-6 text-left">
          <strong>{{ title }}</strong>
        </div>
        <div class="col-2 col-offset-4 text-right">
          <i
            :title="title"
            class="material-icons"
            aria-hidden="true"
            v-tooltip.top="
              tooltipText
                ? {
                    value: tooltipText,
                    class: 'd-tooltip-mw25',
                  }
                : ''
            "
            >info
          </i>
        </div>
        <div class="col-12 text-left">
          <span>{{ humanizedValue }}</span>
        </div>
      </div>
    </template>
  </Card>
</template>

<script>
import Card from "primevue/card";
import Tooltip from "primevue/tooltip";
import { humanizeString } from "@/utils/StringHumanizer";
export default {
  name: "TaxoInfoCard",
  components: { Card },
  directives: {
    tooltip: Tooltip,
  },
  props: {
    title: {
      type: String,
    },
    value: {
      type: String,
    },
    tooltipText: {
      type: String,
      default: "",
    },
  },
  computed: {
    humanizedValue() {
      return this.value !== undefined && this.value !== null ? humanizeString(this.value) : "No data has been reported";
    },
  },
};
</script>
