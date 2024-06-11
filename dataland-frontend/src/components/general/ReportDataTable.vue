<template>
  <div class="p-datatable p-component">
    <div class="p-datatable-wrapper overflow-auto">
      <table v-if="reportData" class="p-datatable-table" aria-label="Data point content">
        <tbody class="p-datatable-body">
          <tr>
            <th class="headers-bg width-auto"><span class="table-left-label">Data source</span></th>
            <td>
              <DocumentLink
                :download-name="reportData.reportName"
                :fileReference="reportData.reportReference"
                data-type=""
                font-style="font-semibold"
                show-icon
              />
            </td>
          </tr>
          <tr v-if="reportData.reportDate">
            <th class="headers-bg width-auto"><span class="table-left-label">Report date</span></th>
            <td>{{ reportData.reportDate }}</td>
          </tr>
          <tr v-if="reportData.reportCurrency">
            <th class="headers-bg width-auto"><span class="table-left-label">Report currency</span></th>
            <td>{{ reportData.reportCurrency }}</td>
          </tr>
          <tr v-if="reportData.reportGroupLevel !== undefined">
            <th class="headers-bg width-auto"><span class="table-left-label">Group level report?</span></th>
            <td>{{ reportData.reportGroupLevel ? "Yes" : "No" }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import DocumentLink from "@/components/resources/frameworkDataSearch/DocumentLink.vue";

export default defineComponent({
  components: { DocumentLink },
  inject: ["dialogRef"],
  name: "ReportDataTable",
  data() {
    return {
      reportData: {
        reportName: undefined as string | undefined,
        reportReference: undefined as string | undefined,
        reportDate: undefined as string | undefined,
        reportCurrency: undefined as string | undefined,
        reportGroupLevel: undefined as boolean | undefined,
      },
    };
  },
  mounted() {
    const dialogRefToDisplay = this.dialogRef;
    const dialogRefData = dialogRefToDisplay.data;
    this.reportData = {
      reportName: dialogRefData.reportName,
      reportReference: dialogRefData.reportReference,
      reportDate: dialogRefData.reportDate,
      reportCurrency: dialogRefData.reportCurrency,
      reportGroupLevel: dialogRefData.reportGroupLevel,
    };
  },
});
</script>

<style scoped lang="scss">
.p-datatable-table {
  border-spacing: 0;
  border-collapse: collapse;
}
.width-auto {
  width: auto;
}
</style>
