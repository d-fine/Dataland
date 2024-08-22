<template>
  <FormKit type="group" :name="name" :label="name">
    <FormKit type="text" name="streetAndHouseNumber" placeholder="Street, House number" />
    <div class="next-to-each-other">
      <div class="formkit-outer shortish-hard">
        <SingleSelectFormElement
          name="country"
          validation-label="Country"
          :validation="required"
          placeholder="Country"
          :options="allCountry"
          data-test="country"
          input-class="long normal-line-height"
        />
      </div>
      <FormKit type="text" name="city" validation-label="City" :validation="required" placeholder="City" />
      <FormKit type="text" name="postalCode" placeholder="Postal Code" />
    </div>
  </FormKit>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { FormKit } from '@formkit/vue';
import { DropdownDatasetIdentifier, getDataset } from '@/utils/PremadeDropdownDatasets';
import SingleSelectFormElement from '@/components/forms/parts/elements/basic/SingleSelectFormElement.vue';

export default defineComponent({
  name: 'AddressFormElement',
  components: { SingleSelectFormElement, FormKit },
  data() {
    return {
      allCountry: getDataset(DropdownDatasetIdentifier.CountryCodesIso2),
    };
  },
  computed: {
    required() {
      if (this.validation === '' || this.validation === 'required') {
        return this.validation;
      } else {
        throw new TypeError("Address form element only accepts '' or 'required' validation");
      }
    },
  },
  props: {
    name: {
      type: String,
      default: '',
    },
    validation: {
      type: String,
      default: '',
    },
  },
});
</script>
