import BigDecimalExtendedDataPointFormField from '@/components/resources/dataTable/modals/BigDecimalExtendedDataPointFormField.vue';
import YesNoExtendedDataPointFormField from '@/components/resources/dataTable/modals/YesNoExtendedDataPointFormField.vue';
import PercentageExtendedDataPointFormField from '@/components/resources/dataTable/modals/PercentageExtendedDataPointFormField.vue';
import CurrencyExtendedDataPointFormField from '@/components/resources/dataTable/modals/CurrencyExtendedDataPointFormField.vue';
import type { Component } from 'vue';

export const componentDictionary: Record<string, Component> = {
  BigDecimalExtendedDataPointFormField,
  YesNoExtendedDataPointFormField,
  PercentageExtendedDataPointFormField,
  CurrencyExtendedDataPointFormField,
};
