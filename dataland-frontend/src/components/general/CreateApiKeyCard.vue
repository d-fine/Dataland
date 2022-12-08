<template>
  <div>
    <div class="surface-card shadow-1 p-3 border-round-sm border-round">
      <div class="flex justify-content-between mb-3">
        <div>
          <div class="text-900 font-medium text-xl text-left pl-1">API Key info</div>
        </div>

        <div class="pr-1">
          <div class="text-left text-xs ml-1 text-600">Scope</div>
          <div class="flex align-items-center justify-content-center">
            <div class="bg-yellow-100 border-round px-2 border-round-sm m-1">
              <span class="text-yellow-700 text-sm font-semibold">READ</span>
            </div>
            <div class="bg-green-100 border-round px-2 border-round-sm m-1">
              <span class="text-green-700 text-sm font-semibold">WRITE</span>
            </div>
          </div>
        </div>
      </div>
      <div
        :class="{ invalidExpireTime: !isExpireTimeCorrect }"
        class="col-12 flex justify-content-between align-items-end"
      >
        <div class="text-left col-5">
          <label
            for="expireTime"
            :class="{ invalidExpireTimeText: !isExpireTimeCorrect, 'text-900': isExpireTimeCorrect }"
            class="block font-medium mb-2"
          >
            {{ !isExpireTimeCorrect ? "Please select expiration date" : "Expiration" }}
          </label>
          <Dropdown
            id="expireTime"
            v-model="expireTimeDropdown"
            :options="days"
            optionLabel="label"
            optionValue="value"
            placeholder="Select expiry"
            class="w-full custom-dropdown"
            @change="setExpireTimeDays($event)"
          />
        </div>

        <div v-if="expireTimeDropdown === 'custom'" class="col-7 text-right">
          <Calendar inputId="icon" v-model="customDate" :showIcon="true" dateFormat="D, M dd, yy" />
        </div>

        <span
          id="expireTimeWrapper"
          v-if="expireTimeDropdown && expireTimeDropdown !== 'custom'"
          class="block text-600 col-7"
        >
          {{
            expireTimeDropdown === "noExpiry"
              ? `The API Key has no defined expire date`
              : `The API Key will expire on ${formatExpiryDate(expireTimeDropdown)}`
          }}
        </span>
      </div>
    </div>
    <div class="mt-3 text-right">
      <PrimeButton label="CANCEL" @click="$emit('cancelCreate')" class="p-button-outlined text-sm ml-3" />
      <PrimeButton
        id="generateApiKey"
        @click="checkDateAndEmitGenerateApiKey"
        label="GENERATE API KEY"
        class="ml-3"
      ></PrimeButton>
    </div>
  </div>
</template>

<script lang="ts">
import PrimeButton from "primevue/button";
import { defineComponent } from "vue";
import Dropdown from "primevue/dropdown";
import Calendar from "primevue/calendar";
import { formatExpiryDate, calculateDaysFromNow } from "@/utils/DateFormatUtils";

export default defineComponent({
  setup() {
    return { formatExpiryDate, calculateDaysFromNow };
  },
  name: "CreateApiKeyCard",
  components: { PrimeButton, Dropdown, Calendar },
  props: {},
  data: () => ({
    expireTimeDays: 0,
    expireTimeDropdown: "",
    isExpireTimeCorrect: true,
    customDate: 0,
    days: [
      { label: "7 days", value: 7 },
      { label: "30 days", value: 30 },
      { label: "60 days", value: 60 },
      { label: "90 days", value: 90 },
      { label: "Custom...", value: "custom" },
      { label: "No expiry", value: "noExpiry" },
    ],
  }),
  computed: {},
  methods: {
    // TODO invent a better logic
    setExpireTimeDays(event: HTMLSelectElement) {
      if (event.value === "noExpiry") {
        this.expireTimeDays = 0;
      } else if (event.value === "custom" && calculateDaysFromNow(this.customDate) > 0) {
        this.expireTimeDays = calculateDaysFromNow(this.customDate);
      } else {
        this.expireTimeDays = event.value as unknown as number;
      }
      this.isExpireTimeCorrect = true;
    },
    checkDateAndEmitGenerateApiKey() {
      if (this.expireTimeDays && this.expireTimeDays > 0) {
        this.$emit("generateApiKey", this.expireTimeDays);
        return;
      } else if (this.expireTimeDropdown === "noExpiry" && this.expireTimeDays === 0) {
        this.$emit("generateApiKey");
        return;
      } else if (this.expireTimeDropdown === "custom" && this.expireTimeDays > 0) {
        this.$emit("generateApiKey", this.expireTimeDays);
        return;
      } else {
        this.isExpireTimeCorrect = false;
      }
    },
  },
  watch: {
    customDate: function () {
      this.expireTimeDays = calculateDaysFromNow(this.customDate);
      this.isExpireTimeCorrect = true;
    },
  },
});
</script>

<style scoped>
.custom-dropdown {
  border: none !important;
}
.invalidExpireTime {
  border: 1px solid var(--red-600);
}
.invalidExpireTimeText {
  color: var(--red-600);
}
</style>
