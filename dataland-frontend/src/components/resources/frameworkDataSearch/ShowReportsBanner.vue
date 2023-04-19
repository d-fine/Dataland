<template>
  <div style="display: flex; align-content: start">
    <div class="text-left bg-white mb-4 mr-2 p-3 all-sides-blue-border" data-test="reportsBanner">
      <span class="font-bold text-gray-800 mt-0 mb-0 mp-0 ml-0 mr-1" style="font-size: 11pt">
        Data extracted from the company report. Company reports:
      </span>
      <span id="reportList">
        <span v-for="(report, name, index) in reports" :key="index">
          <ReportLink :name="name" :report="report" :index="Number(index)" :reportsNumber="reportsNumber()" />
        </span>
      </span>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import ReportLink from "@/components/resources/frameworkDataSearch/ReportLink.vue";

export default defineComponent({
  name: "ShowReportsBanner",
  components: { ReportLink },
  props: {
    reports: { type: Object, required: true },
  },
  methods: {
    /**
     * @returns the number of reports
     */
    reportsNumber(): number {
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
