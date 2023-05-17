<template>
  <div style="display: flex">
    <div class="text-left bg-white mb-4 mr-2 p-3 all-sides-blue-border" data-test="reportsBanner">
      <span class="font-bold text-gray-800 mt-0 mb-0 mp-0 ml-0 mr-1" style="font-size: 11pt">
        Data extracted from the company report. Company reports:
      </span>
      <span id="reportList">
        <span v-for="(report, name, index) in reports" :key="index">
          <DocumentLink :download-name="name" :reference="report.reference" font-style="font-semibold" />
          <span v-if="index < numberOfReports - 1"> | </span>
        </span>
      </span>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import DocumentLink from "@/components/resources/frameworkDataSearch/DocumentLink.vue";

export default defineComponent({
  name: "ShowReportsBanner",
  components: { DocumentLink },
  props: {
    reports: { type: Object, required: true },
  },
  computed: {
    numberOfReports(): number {
      return Object.keys(this.reports).length;
    },
  },
});
</script>

<style scoped>
a:link {
  color: var(--yellow-700);
}
</style>
