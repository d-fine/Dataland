<template>
  <FreeTextFormField name="siteName" label="Site Name" description="Please name the site" validation="required" />
  <AddressFormField
    data-test="AddressFormField"
    name="addressOfSite"
    description="Please state the address of the site."
    label="Site Address"
    validation="required"
  />
  <NumberFormField
    :name="'siteGeocoordinateLongitudeval'"
    validation-label="longitudeval geocoordinate"
    label="Longitudeval Geocoordinate"
    description="Please specify the longitudeval geocoordinate of the site"
    input-class="col-4 pr-0"
  />
  <NumberFormField
    :name="'siteGeocoordinateLatitude'"
    validation-label="latitude geocoordinate"
    label="Latitude Geocoordinate"
    description="Please specify the latitude geocoordinate of the site"
    input-class="col-4 pr-0"
  />
  <FreeTextFormField
    name="biodiversitySensitiveArea"
    label="Biodiversity Sensitive Areae"
    description="Please specify the biodiversity sensitive area"
    validation="required"
  />
  <SingleSelectFormElement
    name="country"
    validation-label="Country"
    :validation="required"
    placeholder="Country"
    :options="allCountry"
    data-test="country"
    input-class="long normal-line-height"
  />
  <NumberFormField
    :name="'areaInHectare'"
    validation-label="area in hectare"
    label="Area in Hectare"
    description="Please specify the longitudeval geocoordinate of the site"
    input-class="col-4 pr-0"
    validation="min:0"
  />

  <AddressFormField
    data-test="AddressFormField"
    name="areaAddress"
    description="Please state the address of the area."
    label="Area Address"
    validation="required"
  />

  <NumberFormField
    :name="'areaGeocoordinateLongitudeval'"
    validation-label="longitudeval geocoordinate"
    label="Longitudeval Geocoordinate"
    description="Please specify the longitudeval geocoordinate of the area"
    input-class="col-4 pr-0"
  />
  <NumberFormField
    :name="'areaGeocoordinateLatitude'"
    validation-label="latitude geocoordinate"
    label="Latitude Geocoordinate"
    description="Please specify the latitude geocoordinate of the area"
    input-class="col-4 pr-0"
  />
  <MultiSelectFormField
    name="adjointness"
    label="Adjointness of the area"
    description="Please specify the adjointness of the area"
    :options="adjointness"
    validation="required"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import FreeTextFormField from "@/components/forms/parts/fields/FreeTextFormField.vue";
import MultiSelectFormField from "@/components/forms/parts/fields/MultiSelectFormField.vue";
import { DropdownDatasetIdentifier, getDataset } from "@/utils/PremadeDropdownDatasets";
import NumberFormField from "@/components/forms/parts/fields/NumberFormField.vue";
import AddressFormField from "@/components/forms/parts/fields/AddressFormField.vue";
import SingleSelectFormElement from "@/components/forms/parts/elements/basic/SingleSelectFormElement.vue";

export default defineComponent({
  name: "SiteAndAreaFormElement",
  data() {
    return {
      releaseMedium: getDataset(DropdownDatasetIdentifier.ReleaseMedium),
      allCountry: getDataset(DropdownDatasetIdentifier.CountryCodesIso2),
      adjointness: getDataset(DropdownDatasetIdentifier.Adjointness),
    };
  },
  components: {
    SingleSelectFormElement,
    AddressFormField,
    NumberFormField,
    MultiSelectFormField,
    FreeTextFormField,
  },
  methods: {},
  //TODO check name, validation, validationLable as those lead to console warnings
});
</script>
