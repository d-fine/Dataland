<template>
  <div data-test="stateHistoryContainer" style="pointer-events: none">
    <Message v-if="!isAdmin" severity="warn" :closable="false" style="margin-bottom: 1rem">
      Non-admin view: Work in progress
    </Message>
    <DataTable :value="combinedHistory" data-test="stateHistoryTable" scrollable class="p-datatable-sm">
      <Column field="timestamp" header="Updated On" style="width: 25%">
        <template #body="{ data }">
          <span data-test="lastModifiedDate">
            {{ convertUnixTimeInMsToDateString(data.timestamp) }}
          </span>
        </template>
      </Column>

      <Column v-if="isAdmin" field="type" header="Type" style="width: 20%">
        <template #body="{ data }">
          <span data-test="historyType">{{ data.type }}</span>
        </template>
      </Column>

      <Column field="state" header="State" :style="isAdmin ? 'width: 25%' : 'width: 35%'">
        <template #body="{ data }">
          <DatalandTag :severity="data.state || ''" :value="data.state" class="dataland-inline-tag" />
        </template>
      </Column>

      <Column field="adminComment" header="Comment" :style="isAdmin ? 'width: 30%' : 'width: 40%'">
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
import { computed } from 'vue';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import type { DataSourcingWithoutReferences, StoredRequest } from '@clients/datasourcingservice';
import DatalandTag from '@/components/general/DatalandTag.vue';
import Message from 'primevue/message';

const props = defineProps<{
  stateHistory: StoredRequest[];
  dataSourcingHistory?: DataSourcingWithoutReferences[];
  isAdmin?: boolean;
}>();

interface CombinedHistoryEntry {
  timestamp: number;
  type: string;
  state: string;
  adminComment?: string | null;
}

const combinedHistory = computed<CombinedHistoryEntry[]>(() => {
  const requestEntries: CombinedHistoryEntry[] = props.stateHistory.map((entry) => ({
    timestamp: entry.lastModifiedDate,
    type: 'Request',
    state: entry.state || '',
    adminComment: entry.adminComment,
  }));

  const dataSourcingEntries: CombinedHistoryEntry[] = (props.dataSourcingHistory || []).map((entry) => ({
    timestamp: entry.lastModifiedDate || 0,
    type: 'Data Sourcing',
    state: entry.state || '',
    adminComment: entry.adminComment,
  }));

  return [...requestEntries, ...dataSourcingEntries].sort((a, b) => b.timestamp - a.timestamp);
});
</script>
