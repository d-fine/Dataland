<template>
  <div class="mb-3">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <div>
      <MultiSelect
        v-model="selections"
        :options="options"
        :placeholder="label"
        option-label="label"
        option-value="value"
        :show-toggle-all="false"
        class="medium"
      />
      <div class="mt-3">
        <FormKit type="group" name="applicableHighImpactClimateSectors">
          <div
            v-for="selection of selections"
            data-test="applicableHighImpactClimateSector"
            :key="selection"
            class="bordered-box p-3 relative col-12 mb-4"
          >
            <em @click="removeItem(selection)" class="material-icons gray-closeIcon">close</em>
            <h4 class="gray-text fw-normal">{{ options.find((option) => option.value == selection).label }}</h4>
            <div class="grid-2-form-cards">
              <FormKit type="group" :name="`NaceCode${selection}`">
                <div>
                  <BigDecimalExtendedDataPointFormField
                    :label="`Sector ${selection} Energy Consumption`"
                    :description="`Total energy consumption for high impact climate sector ${selection}`"
                    :required="false"
                    unit="GWh"
                    name="highImpactClimateSectorEnergyConsumptionInGWh"
                    validation-label="Energy Consumption"
                    inputClass="col-8"
                    :isDataPointToggleable="false"
                  />
                </div>
                <div>
                  <BigDecimalExtendedDataPointFormField
                    :label="`Sector ${selection} Relative Energy Consumption`"
                    :description="`Energy consumption for high impact climate sector ${selection} per revenue`"
                    :required="false"
                    unit="GWh / €M revenue"
                    name="highImpactClimateSectorEnergyConsumptionInGWhPerMillionEURRevenue"
                    validation-label="Relative Energy Consumption"
                    inputClass="col-8"
                    :isDataPointToggleable="false"
                  />
                </div>
              </FormKit>
            </div>
          </div>
        </FormKit>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
// @ts-nocheck
import { defineComponent } from 'vue';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import MultiSelect from 'primevue/multiselect';
import { optionsForHighImpactClimateSectors } from '@/types/HighImpactClimateSectors';
import BigDecimalExtendedDataPointFormField from '@/components/forms/parts/fields/BigDecimalExtendedDataPointFormField.vue';

export default defineComponent({
  name: 'HighImpactClimateSectorsFormField',
  inject: {
    injectClimateSectors: {
      from: 'climateSectorsForPrefill',
      default: [] as string[],
    },
  },
  components: { BigDecimalExtendedDataPointFormField, MultiSelect, UploadFormHeader },
  props: BaseFormFieldProps,
  data() {
    return {
      options: optionsForHighImpactClimateSectors,
      selections: [],
    };
  },
  mounted() {
    this.selections = this.injectClimateSectors;
  },
  methods: {
    /**
     * Unselected item from selections
     * @param selection item to unselect
     */
    removeItem(selection: string) {
      this.selections = this.selections.filter((el: string) => el !== selection);
    },
  },
});
</script>
<style scoped>
.bordered-box {
  border: 2px dotted var(--input-separator);
  padding: 16px;
}

.gray-text {
  color: var(--gray);
}

.grid-2-form-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;

  > div:nth-child(even) {
    border-left: 1px solid var(--table-border);
    padding: 0 0 0 20px;
  }

  > div:nth-child(odd) {
    border: 0 none;
    padding: 0 20px 0 0;
  }

  grid-gap: 10px;
  @media screen and (max-width: 1200px) {
    grid-template-columns: 1fr;
    > div:nth-child(even) {
      border: 0 none;
      padding: 0;
    }
    > div:nth-child(odd) {
      padding: 0;
    }
    > div {
      margin-bottom: 1rem;
    }
  }
}

.gray-closeIcon {
  cursor: pointer;
  position: absolute;
  right: 5px;
  top: 5px;
  color: var(--gray);
}
</style>
