<template>
  <div>
    <div class="surface-card shadow-1 p-3 border-round-sm border-round">
      <div class="flex justify-content-between mb-3">
        <div>
          <div class="text-900 font-medium text-xl text-left">API Key info</div>
        </div>

          <div>
            <div class="text-left text-xs ml-1 text-600">Scope</div>
            <div class="flex align-items-center justify-content-center ">
              <div class="bg-yellow-100 border-round px-2 border-round-sm m-1">
            <span class="text-yellow-700 text-sm font-semibold">READ</span>
          </div>
          <div class="bg-green-100 border-round px-2 border-round-sm m-1">
            <span class="text-green-700 text-sm font-semibold">WRITE</span>
          </div>
          </div>
        </div>
      </div>
      <div class="col-12 flex justify-content-between align-items-center">
        <div class="text-left">
          <FormKit
              type="select"
              label="Expiration"
              name="planet"
              v-model="expireTime"
              placeholder="Select expiry"
              outer-class="date-form"
              :options="[
                { label: '7 days', value: '7' },
                { label: '30 days', value: '30' },
                { label: '60 days', value: '60' },
                { label: '90 days', value: '90' },
                { label: 'Custom...', value: 'custom' },
                { label: 'No expiry', value: 'noExpiry' },
              ]"
          >
          </FormKit>
        </div>

        <div v-if="expireTime === 'custom'">

          <Calendar
              inputId="icon"
              v-model="date3"
              :showIcon="true"
              dateFormat="D, M dd, yy"
          />
        </div>

        <span v-if="expireTime != 'custom'" class="block text-600 mb-1 mt-6">The API Key will expire on {{ expiryDateCalculation }}</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import Button from "primevue/button";
import {defineComponent} from "vue";
import {humanizeString} from "@/utils/StringHumanizer";
import { FormKit } from "@formkit/vue";
import Calendar from 'primevue/calendar';

export default defineComponent({
  name: "CreateApiKeyCard",
  components: { Button, FormKit, Calendar },
  props: {},
  data: () => ({
    expireTime: 0,
    date3: null,
  }),
  computed: {
    expiryDateCalculation() {
      // One day is 24h60min60s*1000ms = 86400000 ms
      const options = { weekday: 'short', year: 'numeric', month: 'short', day: 'numeric' };
      return new Date(Date.now() +(this.expireTime * 86400000)).toLocaleDateString(undefined, options);
    },
  },
});
</script>

<style scoped>
.date-form select.formkit-input { border-color: #000 }
/*719ECE*/
</style>
