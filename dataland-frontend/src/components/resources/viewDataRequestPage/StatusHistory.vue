<template>
  <div>
    <p v-show="!isStatusHistoryVisible" @click="toggleViewStatusHistory()">view status history</p>
  </div>
  <div v-show="isStatusHistoryVisible">
    <table>
      <tr v-for="(statusChange, index) in statusHistoryToDisplay" :key="index" class="">
        <td style="width: 130px; display: flex; align-items: center">
          <div :class="badgeClass(statusChange?.requestStatus)" style="margin: 0 1rem; display: inline-flex">
            {{ statusChange?.requestStatus }}
          </div>
        </td>
        <td>
          <div class="card__subtitle">
            {{ convertUnixTimeInMsToDateString(statusChange?.statusChangeDate) }}
          </div>
        </td>
      </tr>
    </table>
  </div>
  <div>
    <p v-show="isStatusHistoryVisible" @click="toggleViewStatusHistory()">close status history</p>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { convertUnixTimeInMsToDateString } from "@/utils/DataFormatUtils";
import { badgeClass } from "@/utils/RequestUtils";

export default defineComponent({
  name: "StatusHistory",
  props: {
    statusHistory: {
      type: Array,
    },
  },
  computed: {
    statusHistoryToDisplay() {
      return this.statusHistory?.toReversed();
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
