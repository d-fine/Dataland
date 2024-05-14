<template>
  <div
    class="card__data"
    style="cursor: pointer; margin-bottom: 1rem"
    data-test="status_history_toggle"
    @click="toggleViewStatusHistory()"
  >
    <span v-show="!isStatusHistoryVisible">
      Show Request Status History
      <i class="pi pi-caret-down"></i>
    </span>
    <span v-show="isStatusHistoryVisible">
      Hide Request Status History
      <i class="pi pi-caret-down"></i>
    </span>
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
    </span>
    <div class="card__separator" style="margin-top: 0.25rem; margin-bottom: 0.25rem" />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { convertUnixTimeInMsToDateString } from "@/utils/DataFormatUtils";
import { badgeClass } from "@/utils/RequestUtils";
import { type StoredDataRequestStatusObject } from "@clients/communitymanager";
export default defineComponent({
  name: "StatusHistory",
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

<style scoped lang="scss"></style>
