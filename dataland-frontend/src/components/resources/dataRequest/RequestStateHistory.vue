<template>
  <div data-test="stateHistoryContainer" style="pointer-events: none; min-width: 900px">
    <DataTable :value="combinedHistory" data-test="stateHistoryTable" scrollable class="p-datatable-sm">
      <Column field="timestamp" header="Updated On" style="width: 15%">
        <template #body="{ data }">
          <span data-test="lastModifiedDate">
            {{ convertUnixTimeInMsToDateString(data.timestamp) }}
          </span>
        </template>
      </Column>

      <Column field="mixedState" header="State" :style="'width: 20%'">
        <template #body="{ data }">
          <DatalandTag
            :severity="data.state || '-'"
            :value="getDisplayedStateLabel(data.state)"
            class="dataland-inline-tag"
          />
        </template>
      </Column>

      <Column v-if="isAdmin" field="requestState" header="Request State" :style="'width: 20%'">
        <template #body="{ data }">
          <DatalandTag
            :severity="data.state || '-'"
            :value="data.type === 'Request' ? data.state : RequestState.Processing"
            class="dataland-inline-tag"
          />
        </template>
      </Column>

      <Column v-if="isAdmin" field="dataSourcingState" header="Data Sourcing State" :style="'width: 20%'">
        <template #body="{ data }">
          <DatalandTag
            :severity="data.state || '-'"
            :value="data.type === 'Data Sourcing' ? data.state : '-'"
            class="dataland-inline-tag"
          />
        </template>
      </Column>

      <Column field="adminComment" header="Comment" :style="'width: 25%'">
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
import { type DataSourcingWithoutReferences, RequestState, type StoredRequest } from '@clients/datasourcingservice';
import DatalandTag from '@/components/general/DatalandTag.vue';
import { getDisplayedStateLabel } from '@/utils/RequestsOverviewPageUtils.ts';

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

  const combined = [...requestEntries, ...dataSourcingEntries].sort((a, b) => a.timestamp - b.timestamp);

  return combined.filter((entry) => entry.state !== 'Processing');
});
</script>
