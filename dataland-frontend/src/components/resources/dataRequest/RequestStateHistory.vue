<template>
  <div data-test="stateHistoryContainer" style="pointer-events: none">
    <DataTable :value="props.stateHistory" data-test="stateHistoryTable" scrollable class="p-datatable-sm">
      <Column field="lastModifiedDate" header="Updated On" style="width: 30%">
        <template #body="{ data }">
          <span data-test="lastModifiedDate">
            {{ convertUnixTimeInMsToDateString(data.lastModifiedDate) }}
          </span>
        </template>
      </Column>

      <Column field="state" header="Request State" style="width: 25%">
        <template #body="{ data }">
          <DatalandTag :severity="data.state || ''" :value="data.state" class="dataland-inline-tag" />
        </template>
      </Column>

      <Column field="adminComment" header="Comment" style="width: 45%">
        <template #body="{ data }">
          <div style="display: inline-flex" data-test="adminComment">
            {{ data.adminComment || '—' }}
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
import DatalandTag from '@/components/general/DatalandTag.vue';

const props = defineProps<{ stateHistory: StoredRequest[] }>();
</script>
