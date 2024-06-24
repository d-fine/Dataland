<template>
  <div class="form-field">
    <div data-test="dataPointToggle" class="form-field border-none vertical-middle">
      <InputSwitch data-test="dataPointToggleButton" inputId="dataPointIsAvailableSwitch" v-model="isItActive" />
      <h5 data-test="dataPointToggleTitle" class="m-2">
        {{ label }}
      </h5>
    </div>
    <FormKit type="group" :name="name" v-if="isItActive">
      <div data-test="">
        <div class="form-field border-none">
          <PercentageFormField
            label="Drive mix per fleet segment"
            description="Share of alternative drive types per fleet segment"
            :is-required="false"
            v-model:percentageFieldValueBind="driveMixPerFleetSegmentInPercent"
            name="driveMixPerFleetSegmentInPercent"
            validation="between:0,100"
          />
        </div>

        <div class="form-field" data-test="totalAmountOfVehicles">
          <UploadFormHeader label="Total amount of vehicles" description="Total amount of vehicles per fleet segment" />
          <FormKit
            type="text"
            name="totalAmountOfVehicles"
            validation-label="Total amount of vehicles"
            validation="number"
            v-model="totalAmountOfVehicles"
            inner-class="short"
          />
        </div>
      </div>
    </FormKit>
  </div>
</template>

<script lang="ts">
// @ts-nocheck
import { defineComponent } from 'vue';
import InputSwitch from 'primevue/inputswitch';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import { FormKit } from '@formkit/vue';
import PercentageFormField from '@/components/forms/parts/fields/PercentageFormField.vue';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { type DriveMixType } from '@/api-models/DriveMixType';

export default defineComponent({
  name: 'DriveMixFormElement',
  inject: {
    driveMixPerFleetSegment: {
      from: 'driveMixPerFleetSegment',
      default: {} as { [key: string]: DriveMixType },
    },
  },
  components: {
    UploadFormHeader,
    PercentageFormField,
    InputSwitch,
    FormKit,
  },
  props: BaseFormFieldProps,
  data() {
    return {
      driveMixPerFleetSegmentInPercent: '',
      totalAmountOfVehicles: '',
      isItActive: !!this.driveMixPerFleetSegment[this.name],
    };
  },
});
</script>
