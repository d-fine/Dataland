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

<script lang="ts">
import { defineComponent } from "vue";
import { getComponentOptionsForDatalandInfoCard } from "@/components/PredefinedComponentOptionsGetter";

export default defineComponent(getComponentOptionsForDatalandInfoCard("TaxoInfoCard"));
</script>
