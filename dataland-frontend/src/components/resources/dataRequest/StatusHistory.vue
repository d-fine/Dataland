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
  <div
    v-show="isStatusHistoryVisible"
    v-for="requestStatusObject in statusHistory"
    :key="requestStatusObject.creationTimestamp"
  >
    <span style="display: flex; align-items: center">
      <div class="card__subtitle" data-test="creation_timestamp">
        {{ convertUnixTimeInMsToDateString(requestStatusObject.creationTimestamp) }}
      </div>
      <div data-test="request_status" :class="badgeClass(requestStatusObject.status)" style="display: inline-flex">
        {{ requestStatusObject.status }}
      </div>
      <div class="card__subtitle">and</div>
      <div
        data-test="access_status"
        :class="accessStatusBadgeClass(requestStatusObject.accessStatus)"
        style="display: inline-flex"
      >
        {{ requestStatusObject.accessStatus }}
      </div>
    </span>
    <div class="card__separator" style="margin-top: 0.25rem; margin-bottom: 0.25rem" />
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import { accessStatusBadgeClass, badgeClass } from '@/utils/RequestUtils';
import { type StoredDataRequestStatusObject } from '@clients/communitymanager';
import ChevronDownIcon from 'primevue/icons/chevrondown';
import ChevronUpIcon from 'primevue/icons/chevronup';
export default defineComponent({
  name: 'StatusHistory',
  components: { ChevronUpIcon, ChevronDownIcon },
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
