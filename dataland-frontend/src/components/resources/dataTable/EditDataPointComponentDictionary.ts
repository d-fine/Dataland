import BigDecimalExtendedDataPointFormField from '@/components/resources/dataTable/modals/BigDecimalExtendedDataPointFormField.vue';
import YesNoExtendedDataPointFormField from '@/components/resources/dataTable/modals/YesNoExtendedDataPointFormField.vue';
import PercentageExtendedDataPointFormField from '@/components/resources/dataTable/modals/PercentageExtendedDataPointFormField.vue';
import CurrencyExtendedDataPointFormField from '@/components/resources/dataTable/modals/CurrencyExtendedDataPointFormField.vue';
import EligibleOrAlignedActivitiesFormField from '@/components/resources/dataTable/modals/EligibleOrAlignedActivitiesFormField.vue';
import DateExtendedDataPointFormField from '@/components/resources/dataTable/modals/DateExtendedDataPointFormField.vue';
import RadioButtonsExtendedDataPointFormField from '@/components/resources/dataTable/modals/RadioButtonsExtendedDataPointFormField.vue';
import AssuranceFormField from '@/components/resources/dataTable/modals/AssuranceFormField.vue';
import AlignedActivitiesFormField from '@/components/resources/dataTable/modals/AlignedActivitiesFormField.vue';
import NonAlignedActivitiesFormField from '@/components/resources/dataTable/modals/NonAlignedActivitiesFormField.vue';
import type { Component } from 'vue';

export const componentDictionary: Record<string, Component> = {
  BigDecimalExtendedDataPointFormField,
  YesNoExtendedDataPointFormField,
  PercentageExtendedDataPointFormField,
  CurrencyExtendedDataPointFormField,
  EligibleOrAlignedActivitiesFormField,
  DateExtendedDataPointFormField,
  RadioButtonsExtendedDataPointFormField,
  AssuranceFormField,
  AlignedActivitiesFormField,
  NonAlignedActivitiesFormField,
};
