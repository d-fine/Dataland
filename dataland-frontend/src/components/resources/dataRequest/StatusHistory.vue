<template>
  <div
    class="card__data"
    style="cursor: pointer; margin-bottom: 1rem"
    data-test="status_history_toggle"
    @click="toggleViewStatusHistory()"
  >
    <div v-show="!isStatusHistoryVisible" style="display: flex; align-items: center">
      <span> Show Request Status History </span>
      <i class="pi pi-chevron-down p-icon p-row-toggler-icon ml-2" />
    </div>
    <div v-show="isStatusHistoryVisible" style="display: flex; align-items: center">
      <span> Hide Request Status History </span>
      <i class="pi pi-chevron-up p-icon p-row-toggler-icon ml-2" />
    </div>
  </div>

  <div v-show="isStatusHistoryVisible">
    <div>
      <DataTable :value="props.statusHistory" data-test="statusHistoryTable">
        <Column field="creationTimeStamp" header="Creation Timestamp" style="width: 28%"
          ><template #body="slotProps">
            <span data-test="creationTimestampEntry">
              {{ convertUnixTimeInMsToDateString(slotProps.data.creationTimestamp) }}
            </span>
          </template>
        </Column>
        <Column field="requestStatus" header="Request Status"
          ><template #body="slotProps"
            ><div
              style="display: inline-flex"
              :class="badgeClass(slotProps.data.status)"
              data-test="requestStatusEntry"
            >
              {{ slotProps.data.status }}
            </div></template
          >
        </Column>
        <Column field="reasonNonSourceable" header="Comment" style="width: 45%"
          ><template #body="slotProps"
            ><div style="display: inline-flex" data-test="reasonNonSourceableEntry">
              {{ slotProps.data.requestStatusChangeReason }}
            </div></template
          >
        </Column>
      </DataTable>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import { badgeClass } from '@/utils/RequestUtils';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import type {StoredRequest} from "@clients/datasourcingservice";

const props = defineProps<{ statusHistory: StoredRequest[] }>();
const isStatusHistoryVisible = ref(false);

function toggleViewStatusHistory() {
  isStatusHistoryVisible.value = !isStatusHistoryVisible.value;
}
</script>
