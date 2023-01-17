<template>
  <Card class="bg-white d-infocard d-card mr-2">
    <template #title></template>
    <template #content>
      <div class="text-center">
        <slot> </slot>
      </div>
    </template>
  </Card>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { getComponentOptionsForDatalandInfoCard } from "@/components/PredefinedComponentOptionsGetter";

export default defineComponent(getComponentOptionsForDatalandInfoCard("InfoCard"));
</script>
