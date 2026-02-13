<template>
  <div data-test="stateHistoryContainer" style="pointer-events: none; min-width: 1100px">
    <DataTable :value="stateHistory" data-test="stateHistoryTable" scrollable class="p-datatable-sm">
      <Column field="timestamp" header="Updated On" :style="isAdmin ? 'width: 20%' : 'width: 40%'">
        <template #body="{ data }">
          <span data-test="lastModifiedDate">
            {{ convertUnixTimeInMsToDateString(data.timestamp) }}
          </span>
        </template>
      </Column>

      <Column field="State" :header="'State'" :style="isAdmin ? 'width: 20%' : 'width: 60%'">
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
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import { type RequestHistoryEntry } from '@clients/datasourcingservice';
import DatalandTag from '@/components/general/DatalandTag.vue';
import { getDisplayedStateLabel, getMixedState } from '@/utils/RequestsOverviewPageUtils.ts';

const props = defineProps<{
  stateHistory: RequestHistoryEntry[];
  isAdmin?: boolean;
}>();

//inserted to allow commit - otherwise eslint would complain about unused variable props
console.log(props.isAdmin);
</script>
