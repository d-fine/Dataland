<template>
  <div class="form-field">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <FormKit type="group" :name="name">
      <FormKit type="meta" name="currentYear" :value="baseYear" />
      <FormKit type="group" name="yearlyData">
        <div class="flex gap-2">
          <div class="flex flex-column justify-content-around pt-5">
            <div v-for="row of options" :key="row.value">
              <h5>{{ row.label }}</h5>
            </div>
          </div>
          <div v-for="group in groupsToDisplay" :key="group.headerTitle">
            <div :class="['p-badge', `badge-${group.color}`, 'flex', 'align-items-center']">
              <em v-if="group.icon" class="material-icons-outlined pr-1">{{ group.icon }}</em>
              {{ group.headerTitle }}
            </div>
            <div class="flex gap-2">
              <div class="flex flex-column justify-content-between" v-for="year of group.yearsInOrder" :key="year">
                <FormKit type="group" :name="year">
                  <div v-for="row of options">
                    <h6>{{ year }}</h6>
                    <FormKit type="text" :name="row.value" />
                  </div>
                </FormKit>
              </div>
            </div>
          </div>
        </div>
      </FormKit>
    </FormKit>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";

import { DropdownOptionFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import { FormKit } from "@formkit/vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";

interface YearGroup {
  headerTitle: string;
  color: "light-gray" | "dark-blue";
  icon?: string;
  yearsInOrder: number[];
}

export default defineComponent({
  name: "GdvYearlyDecimalTimeseriesDataFormField",
  components: { UploadFormHeader, FormKit },
  props: {
    ...DropdownOptionFormFieldProps,
  },
  computed: {
    baseYear() {
      return 2022;
    },
    groupsToDisplay(): YearGroup[] {
      return [
        {
          headerTitle: "Historical Data",
          color: "light-gray",
          icon: "menu_book",
          yearsInOrder: [2019, 2020, 2021],
        },
        {
          headerTitle: "Reporting year",
          color: "dark-blue",
          yearsInOrder: [2022],
        },
        {
          headerTitle: "Prognosis Data",
          color: "light-gray",
          icon: "lightbulb",
          yearsInOrder: [2023, 2024, 2025],
        },
      ];
    },
  },
});
</script>

<style scoped lang="scss">
@import "src/assets/scss/variables.scss";
.badge-light-gray {
  color: $aquamarine-dark;
  background: $paper-white;

  .material-icons-outlined {
    font-size: 1rem;
  }
}

.badge-dark-blue {
  background: $aquamarine-dark;
  color: $paper-white;
}
</style>
