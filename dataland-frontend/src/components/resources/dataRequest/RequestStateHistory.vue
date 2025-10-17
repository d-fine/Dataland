<template>
  <div data-test="stateHistoryContainer">
    <DataTable
        :value="props.stateHistory"
        data-test="stateHistoryTable"
        responsiveLayout="scroll"
        class="p-datatable-sm"
    >
      <Column
          field="lastModifiedDate"
          header="Date"
          style="width: 30%"
      >
        <template #body="slotProps">
      <span data-test="creationTimestampEntry">
        {{ convertUnixTimeInMsToDateString(slotProps.data.lastModifiedDate) }}
      </span>
        </template>
      </Column>

      <Column
          field="state"
          header="Request State"
          style="width: 25%"
      >
        <template #body="slotProps">
          <DatalandTag
              :severity="slotProps.data.state || ''"
              :value="slotProps.data.state"
              class="dataland-inline-tag"
          />
        </template>
      </Column>

      <Column
          field="adminComment"
          header="Comment"
          style="width: 45%"
      >
        <template #body="slotProps">
          <div style="display: inline-flex" data-test="commentEntry">
            {{ slotProps.data.adminComment || '—' }}
          </div>
        </template>
      </Column>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import type { StoredRequest } from '@clients/datasourcingservice';
import DatalandTag from "@/components/general/DatalandTag.vue";

const props = defineProps<{ stateHistory: StoredRequest[] }>();
</script>
