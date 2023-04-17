<template>
  <div class="col-12 text-left bg-white mb-4 mp-0 all-sides-blue-border" data-test="reportsBanner" style="width: 100%">
    <p class="font-bold text-gray-800 mt-0 mb-0 mp-0 ml-0 mr-0" style="font-size: 11pt">
      Data extracted from the company report. Company reports:
    </p>
    <span id="reportList">
      <span v-for="(report, name, index) in reports" :key="index">
        <ReportLink :name="name" :report="report" :index="Number(index)" :reportsNumber="reportsNumber()" />
      </span>
    </span>
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
