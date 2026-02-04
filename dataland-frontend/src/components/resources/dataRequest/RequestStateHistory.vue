<template>
  <div data-test="stateHistoryContainer" style="pointer-events: none; min-width: 1100px">
    <DataTable :value="combinedHistory" data-test="stateHistoryTable" scrollable class="p-datatable-sm">
      <Column field="timestamp" header="Updated On" :style="isAdmin ? 'width: 20%' : 'width: 25%'">
        <template #body="{ data }">
          <span data-test="lastModifiedDate">
            {{ convertUnixTimeInMsToDateString(data.timestamp) }}
          </span>
        </template>
      </Column>

      <Column
        field="mixedState"
        :header="isAdmin ? 'Mixed State' : 'State'"
        :style="isAdmin ? 'width: 20%' : 'width: 30%'"
      >
        <template #body="{ data }">
          <DatalandTag
            :severity="getMixedStatus(data.requestState, data.dataSourcingState) || '-'"
            :value="getDisplayedStateLabel(getMixedStatus(data.requestState, data.dataSourcingState))"
            class="dataland-inline-tag"
          />
        </template>
      </Column>

      <Column v-if="isAdmin" field="requestState" header="Request State" :style="'width: 15%'">
        <template #body="{ data }">
          <DatalandTag :severity="data.requestState || '-'" :value="data.requestState" class="dataland-inline-tag" />
        </template>
      </Column>

      <Column v-if="isAdmin" field="dataSourcingState" header="Data Sourcing State" :style="'width: 20%'">
        <template #body="{ data }">
          <DatalandTag
            :severity="data.dataSourcingState || ''"
            :value="data.dataSourcingState || ''"
            class="dataland-inline-tag"
          />
        </template>
      </Column>

      <Column field="adminComment" header="Comment" :style="isAdmin ? 'width: 25%' : 'width: 45%'">
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
import {
  type DataSourcingWithoutReferences,
  RequestState,
  type StoredRequest,
  type DataSourcingState,
} from '@clients/datasourcingservice';
import DatalandTag from '@/components/general/DatalandTag.vue';
import { getDisplayedStateLabel } from '@/utils/RequestsOverviewPageUtils.ts';

/**
 * Determines the mixed status based on request state and data sourcing state.
 * If the request state is 'Withdrawn' or 'Open', or if data sourcing state is null,
 * it returns the request state. Otherwise, it returns the data sourcing state.
 *
 * @param requestState - The current state of the request.
 * @param dataSourcingState - The current state of data sourcing, which can be null.
 * @returns The mixed status as either RequestState or DataSourcingState.
 */
function getMixedStatus(
  requestState: RequestState,
  dataSourcingState: DataSourcingState | null
): RequestState | DataSourcingState {
  if (requestState == RequestState.Withdrawn || requestState == RequestState.Open || dataSourcingState == null) {
    return requestState;
  } else {
    return dataSourcingState;
  }
}

const props = defineProps<{
  stateHistory: StoredRequest[];
  dataSourcingHistory?: DataSourcingWithoutReferences[];
  isAdmin?: boolean;
}>();

interface CombinedHistoryEntry {
  timestamp: number;
  type: string;
  requestState: string | null;
  dataSourcingState: string | null;
  adminComment?: string | null;
}

const combinedHistory = computed<CombinedHistoryEntry[]>(() => {
  const requestEntries: CombinedHistoryEntry[] = props.stateHistory.map((entry) => ({
    timestamp: entry.lastModifiedDate,
    type: 'Request',
    requestState: entry.state,
    dataSourcingState: null,
    adminComment: entry.adminComment,
  }));

  const dataSourcingEntries: CombinedHistoryEntry[] = (props.dataSourcingHistory || []).map((entry) => ({
    timestamp: entry.lastModifiedDate || 0,
    type: 'Data Sourcing',
    requestState: null,
    dataSourcingState: entry.state,
    adminComment: entry.adminComment,
  }));

  const combined = [...requestEntries, ...dataSourcingEntries].sort((a, b) => a.timestamp - b.timestamp);

  // Fill null values with previous non-null value
  let lastRequestState: string | null = null;
  let lastDataSourcingState: string | null = null;
  let currentAdminComment: string | null | undefined = undefined;
  for (const entry of combined) {
    if (entry.requestState == null) {
      entry.requestState = lastRequestState;
    } else {
      currentAdminComment = entry.adminComment;
      lastRequestState = entry.requestState;
    }
    if (entry.dataSourcingState == null) {
      entry.dataSourcingState = lastDataSourcingState;
    } else {
      if (currentAdminComment !== undefined) {
        entry.adminComment = currentAdminComment;
      }
      lastDataSourcingState = entry.dataSourcingState;
    }
  }

  return combined.filter((entry) => !(entry.dataSourcingState === null && entry.requestState === 'Processing'));
});
</script>
