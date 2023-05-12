<template>
  <FormKit type="group" :name="name" :label="name">
    <FormKit
      type="text"
      name="streetAndHouseNumber"
      :validation="required"
      validation-label="Street and house number"
      placeholder="Street, House number"
    />
    <div class="next-to-each-other">
      <FormKit
        type="select"
        name="country"
        validation-label="Country"
        :validation="required"
        placeholder="Country"
        :options="allCountry"
      />
      <FormKit type="text" name="city" validation-label="City" :validation="required" placeholder="City" />
      <FormKit
        type="text"
        :validation="required"
        validation-label="Postcode"
        name="postalCode"
        placeholder="Postal Code"
      />
    </div>
  </FormKit>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { getAllCountryNamesWithCodes } from "@/utils/CountryCodeConverter";
import { FormKit } from "@formkit/vue";

export default defineComponent({
  name: "AddressFormElement",
  components: { FormKit },
  data() {
    return {
      allCountry: getAllCountryNamesWithCodes(),
    };
  },
  computed: {
    required() {
      if (this.validation === "" || this.validation === "required") {
        return this.validation;
      } else {
        throw new TypeError("Address form element only accepts '' or 'required' validation");
      }
    },
  },
  props: {
    name: {
      type: String,
      default: "",
    },
    validation: {
      type: String,
      default: "",
    },
  },
});
</script>
