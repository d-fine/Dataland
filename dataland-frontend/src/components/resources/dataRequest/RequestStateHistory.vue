<template>
  <div data-test="stateHistoryContainer" style="pointer-events: none; min-width: 1100px">
    <DataTable :value="combinedHistory" data-test="stateHistoryTable" scrollable class="p-datatable-sm">
      <Column field="timestamp" header="Updated On" :style="isAdmin ? 'width: 20%' : 'width: 40%'">
        <template #body="{ data }">
          <span data-test="lastModifiedDate">
            {{ convertUnixTimeInMsToDateString(data.timestamp) }}
          </span>
        </template>
      </Column>

      <Column
        field="mixedState"
        :header="isAdmin ? 'Mixed State' : 'State'"
        :style="isAdmin ? 'width: 20%' : 'width: 60%'"
      >
        <template #body="{ data }">
          <DatalandTag
            :severity="getMixedState(data.requestState, data.dataSourcingState) || '-'"
            :value="getDisplayedStateLabel(getMixedState(data.requestState, data.dataSourcingState))"
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

      <Column v-if="isAdmin" field="adminComment" header="Comment" :style="'width: 25%'">
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
import { getDisplayedStateLabel, getMixedState } from '@/utils/RequestsOverviewPageUtils.ts';

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

type HistoryAccumulator = {
  lastRequestState: string | null;
  lastDataSourcingState: string | null;
  currentAdminComment: string | null | undefined;
};

/**
 * Fills in missing requestState and dataSourcingState values in the combined history entries.
 * It iterates through the entries and uses the last known states to fill in any gaps.
 *
 * @param entries - The combined history entries to be processed.
 * @returns The combined history entries with filled gaps.
 */
function fillHistoryGaps(entries: CombinedHistoryEntry[]): CombinedHistoryEntry[] {
  const acc: HistoryAccumulator = {
    lastRequestState: null,
    lastDataSourcingState: null,
    currentAdminComment: undefined,
  };

  for (const entry of entries) {
    if (entry.requestState == null) {
      entry.requestState = acc.lastRequestState;
    } else {
      acc.currentAdminComment = entry.adminComment;
      acc.lastRequestState = entry.requestState;
    }

    if (entry.dataSourcingState == null) {
      entry.dataSourcingState = acc.lastDataSourcingState;
    } else {
      entry.adminComment = acc.currentAdminComment;
      acc.lastDataSourcingState = entry.dataSourcingState;
    }
  }
  return entries;
}

/**
 * Filters out entries that are in a transient state, such as 'Processing' without a data sourcing state
 * or 'DataVerification' with a 'Processed' request state. These states are not meaningful for display
 * and can be confusing to users.
 *
 * @param entries - The combined history entries to be filtered.
 * @returns The filtered combined history entries.
 */
function filterHistory(entries: CombinedHistoryEntry[]): CombinedHistoryEntry[] {
  return entries.filter(
    (entry) =>
      !(
        (entry.dataSourcingState === null && entry.requestState === 'Processing') ||
        (entry.dataSourcingState === 'DataVerification' && entry.requestState === 'Processed')
      )
  );
}

/**
 * Compacts the history entries by removing consecutive entries that have the same displayed mixed state.
 * This is only useful for non-admin users since they have no information about requestState and dataSourcingState, so
 * consecutive entries with the same mixed state would be redundant.
 *
 * @param entries - The combined history entries to be compacted.
 * @returns The compacted combined history entries.
 */
function compactHistory(entries: CombinedHistoryEntry[]): CombinedHistoryEntry[] {
  const compacted: CombinedHistoryEntry[] = [];
  let lastMixedState: string | null = null;

  for (const entry of entries) {
    const requestState = entry.requestState ?? RequestState.Processing;
    const currentMixedState = getDisplayedStateLabel(
      getMixedState(requestState as RequestState, entry.dataSourcingState as DataSourcingState | null)
    );
    if (currentMixedState !== lastMixedState) {
      compacted.push(entry);
      lastMixedState = currentMixedState;
    }
  }

  return compacted;
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
  const filled = fillHistoryGaps(combined);
  const filtered = filterHistory(filled);

  if (props.isAdmin) {
    return filtered;
  }

  return compactHistory(filtered);
});
</script>
