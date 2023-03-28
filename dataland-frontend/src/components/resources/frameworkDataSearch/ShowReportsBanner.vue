<template>
  <div class="col-11 text-left bg-white mb-4 mp-0 all-sides-blue-border" style="padding-right: 50pt">
    <p class="font-bold text-gray-800 mt-0 mb-0 mp-0 ml-0 mr-0" style="font-size: 11pt">
      Data extracted from the company report. Company reports: {{ listReports }}
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
  computed: {
    listReports() {
      let reportsString = "";
      if (this.reports != undefined) {
        Object.keys(this.reports).forEach(function (name) {
          reportsString = reportsString + name + ", ";
        });
      }
      //const link = rep.reference; //should become link to document manager endpoint let link = documentManagerApi.getDocument(rep.reference)
      return reportsString;
    },
  },
});
</script>

<style>
.d-progressbar {
  height: 0.25rem;
  border-radius: 0.25rem;
}

.d-card {
  border-radius: 0.5rem;
  box-shadow: 0 0 32px 8px rgba(30, 30, 31, 0.08);
}

.d-card > .p-card-body > .p-card-content {
  padding: 0;
}
</style>
