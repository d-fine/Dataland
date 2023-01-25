<template>
  <div>
    <div class="surface-card shadow-1 p-3 border-round-sm border-round">
      <div class="flex justify-content-between mb-3">
        <div>
          <div class="text-900 font-medium text-xl text-left pl-1">API Key info</div>
        </div>
        <div data-test="userRoles" class="pr-1">
          <UserRolesBadges :userRoles="userRoles" />
        </div>
      </div>
      <div
        :class="{ invalidExpiryTime: !isExpiryDateValid }"
        class="col-12 flex justify-content-between align-items-end"
      >
        <div class="text-left col-5">
          <label
            for="expiryTime"
            :class="{ invalidExpiryTimeText: !isExpiryDateValid, 'text-900': isExpiryDateValid }"
            class="block font-medium mb-2"
          >
            {{ !isExpiryDateValid ? "Please select expiration date" : "Expiration" }}
          </label>
          <Dropdown
            id="expiryTime"
            v-model="expiryTimeDropdown"
            :options="days"
            optionLabel="label"
            optionValue="value"
            placeholder="Select expiry"
            class="w-full custom-dropdown"
            @change="setExpiryTimeDays($event)"
          />
        </div>

        <div v-if="expiryTimeDropdown === 'custom'" class="col-7 text-right">
          <Calendar
            data-test="expiryDatePicker"
            inputId="icon"
            v-model="customDateInMilliseconds"
            :showIcon="true"
            dateFormat="D, M dd, yy"
            :minDate="minDate"
          />
        </div>

        <span
          id="expiryTimeWrapper"
          v-if="expiryTimeDropdown && expiryTimeDropdown !== 'custom'"
          class="block text-600 col-7"
        >
          {{
            expiryTimeDropdown === "noExpiry"
              ? `The API Key has no defined expiry date`
              : `The API Key will expire on ${expiryDateFormated}`
          }}
        </span>
      </div>
    </div>
    <div class="mt-3 text-right">
      <PrimeButton
        data-test="cancelGenerateApiKey"
        label="CANCEL"
        @click="$emit('cancelCreate')"
        class="p-button-outlined ml-3"
      />
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
import { calculateExpiryDateAsDateString, calculateDaysFromNow } from "@/utils/DateFormatUtils";
import UserRolesBadges from "@/components/general/apiKey/UserRolesBadges.vue";
import { assertDefined } from "@/utils/TypeScriptUtils";

export default defineComponent({
  name: "CreateApiKeyCard",
  components: { PrimeButton, Dropdown, Calendar, UserRolesBadges },
  props: {
    userRoles: {
      type: Array,
    },
  },
  data: () => ({
    expiryTimeDays: null as null | number,
    expiryTimeDropdown: "",
    isExpiryDateValid: true,
    minDate: new Date(new Date().getTime() + 86400000),
    customDateInMilliseconds: null as null | number,
    days: [
      { label: "7 days", value: 7 },
      { label: "30 days", value: 30 },
      { label: "60 days", value: 60 },
      { label: "90 days", value: 90 },
      { label: "Custom...", value: "custom" },
      { label: "No expiry", value: "noExpiry" },
    ],
  }),
  methods: {
    setExpiryTimeDays(event: HTMLSelectElement) {
      if (event.value === "noExpiry") {
        this.expiryTimeDays = null;
      } else if (event.value === "custom") {
        this.expiryTimeDays = this.customDateInMilliseconds
          ? calculateDaysFromNow(this.customDateInMilliseconds)
          : null;
      } else {
        this.expiryTimeDays = parseInt(event.value);
      }
      this.isExpiryDateValid = true;
    },

    checkDateAndEmitGenerateApiKey() {
      if (this.expiryTimeDays) {
        this.$emit("generateApiKey", this.expiryTimeDays);
      } else if (this.expiryTimeDropdown === "noExpiry") {
        this.$emit("generateApiKey");
      } else {
        this.isExpiryDateValid = false;
      }
    },
  },
  computed: {
    expiryDateFormated(): string {
      return calculateExpiryDateAsDateString(assertDefined(this.expiryTimeDays));
    },
  },
  watch: {
    customDateInMilliseconds: function (newValue: number) {
      this.expiryTimeDays = calculateDaysFromNow(newValue);
      this.isExpiryDateValid = true;
    },
  },
});
</script>

<style scoped>
.invalidExpiryTime {
  border: 1px solid var(--red-600);
}
.invalidExpiryTimeText {
  color: var(--red-600);
}
</style>
