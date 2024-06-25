<template>
  <div class="p-datatable p-component">
    <div class="p-datatable-wrapper overflow-auto">
      <table v-if="companyReport" class="p-datatable-table" aria-label="Data point content">
        <tbody class="p-datatable-body">
          <tr>
            <th class="headers-bg width-auto"><span class="table-left-label">Data source</span></th>
            <td>
              <DocumentLink
                :download-name="companyReport.fileName ? companyReport.fileName : 'Unnamed_File'"
                :fileReference="companyReport.fileReference"
                data-type=""
                font-style="font-semibold"
                show-icon
              />
            </td>
          </tr>
          <tr>
            <th class="headers-bg width-auto"><span class="table-left-label">Publication date</span></th>
            <td>{{ companyReport.publicationDate }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import DocumentLink from '@/components/resources/frameworkDataSearch/DocumentLink.vue';
import type { CompanyReport } from '@clients/backend';

export default defineComponent({
  components: { DocumentLink },
  inject: ['dialogRef'],
  name: 'ReportDataTable',
  data() {
    return {
      companyReport: undefined as CompanyReport | undefined,
    };
  },
  mounted() {
    const dialogRefToDisplay = this.dialogRef as { data: { companyReport: CompanyReport } };
    const dialogRefData = dialogRefToDisplay.data as {
      companyReport: CompanyReport;
    };
    this.companyReport = dialogRefData.companyReport;
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
