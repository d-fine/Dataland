<template>
  <div
    class="card__data"
    style="cursor: pointer; margin-bottom: 1rem"
    data-test="status_history_toggle"
    @click="toggleViewStatusHistory()"
  >
    <div v-show="!isStatusHistoryVisible" style="display: flex; align-items: center">
      <span> Show Request Status History </span>
      <ChevronDownIcon class="p-icon p-row-toggler-icon ml-2" />
    </div>
    <div v-show="isStatusHistoryVisible" style="display: flex; align-items: center">
      <span> Hide Request Status History </span>
      <ChevronUpIcon class="p-icon p-row-toggler-icon ml-2" />
    </div>
  </div>

  <div v-show="isStatusHistoryVisible">
    <div>
      <DataTable :value="statusHistory" data-test="statusHistoryTable">
        <Column field="creationTimeStamp" header="Creation Timestamp" style="width: 33%"
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
              {{ getRequestStatusLabel(slotProps.data.status) }}
            </div></template
          >
        </Column>
        <Column field="accessStatus" header="Access Status"
          ><template #body="slotProps"
            ><div
              style="display: inline-flex"
              :class="accessStatusBadgeClass(slotProps.data.accessStatus)"
              data-test="accessStatusEntry"
            >
              {{ slotProps.data.accessStatus }}
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

<script lang="ts">
import { defineComponent } from 'vue';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import { accessStatusBadgeClass, badgeClass, getRequestStatusLabel } from '@/utils/RequestUtils';
import { type StoredDataRequestStatusObject } from '@clients/communitymanager';
import ChevronDownIcon from 'primevue/icons/chevrondown';
import ChevronUpIcon from 'primevue/icons/chevronup';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
export default defineComponent({
  name: 'StatusHistory',
  components: { ChevronUpIcon, ChevronDownIcon, DataTable, Column },
  props: {
    statusHistory: {
      type: Array<StoredDataRequestStatusObject>,
      required: true,
    },
  },
  data() {
    return {
      isStatusHistoryVisible: false,
    };
  },
  methods: {
    getRequestStatusLabel,
    accessStatusBadgeClass,
    convertUnixTimeInMsToDateString,
    badgeClass,
    /**
     * Toggles whether the status history and the corresponding buttons are visible
     */
    toggleViewStatusHistory() {
      this.isStatusHistoryVisible = !this.isStatusHistoryVisible;
    },
  },
});
</script>
