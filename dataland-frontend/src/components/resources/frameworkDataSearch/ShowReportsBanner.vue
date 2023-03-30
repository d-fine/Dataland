<template>
  <div class="col-12 text-left bg-white mb-4 mp-0 all-sides-blue-border" style="width: 99%">
    <p class="font-bold text-gray-800 mt-0 mb-0 mp-0 ml-0 mr-0" style="font-size: 11pt">
      Data extracted from the company report. Company reports:
    </p>
    <p id="reportList">
      {{ resetReportsCount() }}
      <span v-for="[name, rep] in Object.entries(reports)" :key="name">
        {{ reportPlus() }}
        <a :href="rep.reference">{{ name }}</a> <span v-if="reportCounter < Object.keys(reports).length">, </span>
      </span>
    </p>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { CompanyReport } from "@clients/backend";

export default defineComponent({
  name: "ShowReportsBanner",
  props: {
    reports: { type: Map<string, CompanyReport> },
  },
  data() {
    return {
      reportCounter: 10,
      test: "test",
    };
  },
  computed: {},
  methods: {
    reportPlus() {
      this.reportCounter++;
    },
    resetReportsCount() {
      this.reportCounter = 0;
    },
    getReferencedDocument(hash: string) {
      return 0;
    },
  },
});
</script>

<style>
a:link {
  color: var(--yellow-700);
  text-decoration: none;
}
</style>
