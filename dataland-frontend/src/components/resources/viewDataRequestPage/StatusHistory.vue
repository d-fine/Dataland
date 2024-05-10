<template>
  <div>
    <PrimeButton @click="toggleViewStatusHistory()"> view status history </PrimeButton>
  </div>
  <div v-show="isStatusHistoryVisible">
    <table>
      <tr v-for="(statusChange, index) in statusHistoryToDisplay" :key="index" class="">
        <td>
          <div class="card__subtitle">
            {{ convertUnixTimeInMsToDateString(statusChange?.statusChangeDate) }}
          </div>
        </td>
        <td>
          <div :class="badgeClass(statusChange?.requestStatus)" style="display: inline-flex">
            {{ statusChange?.requestStatus }}
          </div>
        </td>
      </tr>
    </table>
  </div>
</template>

<script lang="ts">
import PrimeButton from "primevue/button";
import { defineComponent } from "vue";
import { convertUnixTimeInMsToDateString } from "@/utils/DataFormatUtils";
import { badgeClass } from "@/utils/RequestUtils";

export default defineComponent({
  name: "StatusHistory",
  components: {
    PrimeButton,
  },
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
