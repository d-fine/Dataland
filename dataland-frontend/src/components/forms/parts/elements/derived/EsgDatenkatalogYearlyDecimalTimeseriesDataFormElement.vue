<template>
  <div class="form-field">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <FormKit type="group" :name="name" v-if="reportingPeriod">
      <FormKit type="meta" name="currentYear" :value="reportingPeriod" />
      <FormKit type="group" name="yearlyData">
        <div class="flex gap-2">
          <div class="flex flex-column justify-content-around pt-5 pr-3">
            <div v-for="row of options" :key="row.value">
              <h5>{{ row.label }}</h5>
            </div>
          </div>
          <div v-for="group in groupsToDisplay" :key="group.headerTitle">
            <div :class="['p-badge', `badge-${group.color}`, 'flex', 'align-items-center', 'min-w-max']">
              <em v-if="group.icon" class="material-icons-outlined pr-1">{{ group.icon }}</em>
              {{ group.headerTitle }}
            </div>
            <div class="flex gap-2">
              <div class="flex flex-column justify-content-between" v-for="year of group.yearsInOrder" :key="year">
                <FormKit type="group" :name="year">
                  <div v-for="row of options" :key="row.value">
                    <h6 class="mb-1">{{ year }}</h6>
                    <FormKit type="text" :name="row.value" validation="number" />
                  </div>
                </FormKit>
              </div>
            </div>
          </div>
        </div>
      </FormKit>
    </FormKit>
    <div v-else>
      <div class="p-badge flex badge-yellow align-items-center w-max">
        <em class="material-icons-outlined pr-1">warning</em>
        Please specify a reporting year above to fill this field
      </div>
    </div>
  </div>
</template>

<script lang="ts">
// @ts-nocheck
import { defineComponent } from 'vue';

import { DropdownOptionFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import { FormKit } from '@formkit/vue';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';

interface YearGroup {
  headerTitle: string;
  color: 'light-gray' | 'dark-blue';
  icon?: string;
  yearsInOrder: number[];
}

export default defineComponent({
  name: 'EsgDatenkatalogYearlyDecimalTimeseriesDataFormElement',
  components: { UploadFormHeader, FormKit },
  props: {
    ...DropdownOptionFormFieldProps,
    reportingPeriod: {
      type: String,
      required: false,
    },
    nYearsIntoFuture: {
      type: Number,
      required: true,
    },
    nYearsIntoPast: {
      type: Number,
      required: true,
    },
    showCurrentYear: {
      type: Boolean,
      required: true,
    },
  },
  computed: {
    numericalYearOfDataDate(): number | undefined {
      const parsedNumber = parseInt(this.reportingPeriod ?? '');
      if (isNaN(parsedNumber)) return undefined;
      else return parsedNumber;
    },
    pastGroup(): YearGroup | undefined {
      if (!this.nYearsIntoPast || !this.numericalYearOfDataDate) return undefined;

      const pastGroup: YearGroup = {
        headerTitle: 'Historische Daten',
        color: 'light-gray',
        icon: 'menu_book',
        yearsInOrder: [],
      };
      for (let year = this.numericalYearOfDataDate - this.nYearsIntoPast; year < this.numericalYearOfDataDate; year++) {
        pastGroup.yearsInOrder.push(year);
      }
      return pastGroup;
    },
    futureGroup(): YearGroup | undefined {
      if (!this.nYearsIntoFuture || !this.numericalYearOfDataDate) return undefined;
      const futureGroup: YearGroup = {
        headerTitle: 'Prognosen',
        color: 'light-gray',
        icon: 'lightbulb',
        yearsInOrder: [],
      };
      for (
        let year = this.numericalYearOfDataDate + 1;
        year <= this.numericalYearOfDataDate + this.nYearsIntoFuture;
        year++
      ) {
        futureGroup.yearsInOrder.push(year);
      }
      return futureGroup;
    },
    currentGroup(): YearGroup | undefined {
      if (!this.showCurrentYear || !this.numericalYearOfDataDate) return undefined;
      return {
        headerTitle: 'Aktuelles Jahr',
        color: 'dark-blue',
        yearsInOrder: [this.numericalYearOfDataDate],
      };
    },
    groupsToDisplay(): YearGroup[] {
      if (!this.numericalYearOfDataDate) return [];
      const groups = [this.pastGroup, this.currentGroup, this.futureGroup];
      return groups.filter((it) => !!it);
    },
  },
});
</script>

<style scoped lang="scss">
@import 'src/assets/scss/variables.scss';
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
