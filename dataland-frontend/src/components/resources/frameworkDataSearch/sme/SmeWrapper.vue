<template>
    <DynamicDialog />
    <SmePanel :company-id="companyId" :single-data-meta-info-to-display="singleDataMetaInfoToDisplay" />
</template>

<script lang="ts">
import { PanelProps } from "@/components/resources/frameworkDataSearch/PanelComponentOptions";
import { defineComponent } from "vue";
import DynamicDialog from "primevue/dynamicdialog";
import SmePanel from "@/components/resources/frameworkDataSearch/sme/SmePanel.vue";

export default defineComponent({
  name: "SmeWrapper",
  components: { DynamicDialog, SmePanel },
  props: PanelProps,
});
</script>
