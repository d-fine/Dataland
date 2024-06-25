<template>
  <FreeTextFormField name="siteName" label="Site Name" description="Please name the site" validation="required" />
  <AddressFormField
    data-test="AddressFormFieldSite"
    name="siteAddress"
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
  <NumberFormField
    :name="'areaInHectare'"
    validation-label="area in hectare"
    label="Area in Hectare"
    description="Please specify the longitudeval geocoordinate of the site"
    input-class="col-4 pr-0"
    validation="min:0"
  />
  <FreeTextFormField
    name="biodiversitySensitiveArea"
    label="Biodiversity Sensitive Areae"
    description="Please specify the biodiversity sensitive area"
  />
  <AddressFormField
    data-test="AddressFormFieldArea"
    name="areaAddress"
    description="Please state the address of the area."
    label="Area Address"
    validation="required"
  />

  <NumberFormField
    :name="'areaGeocoordinateLongitude'"
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
  <SingleSelectFormField
    name="specificationOfAdjointness"
    label="Adjointness of the area"
    description="Please specify the adjointness of the area"
    :options="adjointness"
    validation="required"
    data-test="specificationOfAdjointness"
  />
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import FreeTextFormField from '@/components/forms/parts/fields/FreeTextFormField.vue';
import { DropdownDatasetIdentifier, getDataset } from '@/utils/PremadeDropdownDatasets';
import NumberFormField from '@/components/forms/parts/fields/NumberFormField.vue';
import AddressFormField from '@/components/forms/parts/fields/AddressFormField.vue';
import SingleSelectFormField from '@/components/forms/parts/fields/SingleSelectFormField.vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';

export default defineComponent({
  name: 'SiteAndAreaFormElement',
  data() {
    return {
      adjointness: getDataset(DropdownDatasetIdentifier.Adjointness),
    };
  },
  components: {
    AddressFormField,
    NumberFormField,
    SingleSelectFormField,
    FreeTextFormField,
  },
  methods: {},
  props: BaseFormFieldProps,
});
</script>
