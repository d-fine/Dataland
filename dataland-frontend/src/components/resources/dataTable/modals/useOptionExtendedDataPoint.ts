import { computed, inject, ref, type ComputedRef, type Ref } from 'vue';
import type { DocumentMetaInfoResponse } from '@clients/documentmanager';
import {
  buildApiBody,
  type ExtendedDataPointMetaInfoType,
  type ExtendedDataPointType,
} from '@/components/resources/dataTable/conversion/Utils.ts';

export interface SelectOption {
  label: string;
  value: string;
}

export interface OptionExtendedDataPoint {
  options: SelectOption[];
  dataPointValue: Ref<string | null>;
  selectedDocumentMeta: Ref<DocumentMetaInfoResponse | undefined>;
  extendedDialogRef: Ref<{ getFormData: () => ExtendedDataPointMetaInfoType; isPageValid: boolean } | undefined>;
  isPageValid: ComputedRef<boolean>;
  buildApiBodyWithExtendedInfo: () => string;
}

/**
 * Shared state and logic of the edit form fields that let the user pick one of several options for a data point.
 * The options are chosen based on the injected data point type ID.
 * @param extendedDataPointObject the data point object used to prefill the form
 * @param optionsByDataPointTypeId the available options per data point type ID
 * @param defaultOptions the options used if the data point type ID has no entry
 * @returns the state and helpers needed by the form field
 */
export function useOptionExtendedDataPoint(
  extendedDataPointObject: ExtendedDataPointType,
  optionsByDataPointTypeId: Record<string, SelectOption[]>,
  defaultOptions: SelectOption[] = []
): OptionExtendedDataPoint {
  const dataPointTypeId = inject<string | undefined>('dataPointTypeId', undefined);
  const options = optionsByDataPointTypeId[dataPointTypeId ?? ''] ?? defaultOptions;

  const initialValue = extendedDataPointObject.value;
  const dataPointValue = ref<string | null>(
    typeof initialValue === 'string'
      ? (options.find((option) => option.value === initialValue || option.label === initialValue)?.value ?? null)
      : null
  );
  const selectedDocumentMeta = ref<DocumentMetaInfoResponse | undefined>(undefined);
  const extendedDialogRef = ref<{ getFormData: () => ExtendedDataPointMetaInfoType; isPageValid: boolean }>();
  const isPageValid = computed<boolean>(() => extendedDialogRef.value?.isPageValid ?? true);

  /**
   * @returns the JSON-stringified API body including the extended meta info
   */
  function buildApiBodyWithExtendedInfo(): string {
    return buildApiBody(dataPointValue.value, undefined, extendedDialogRef.value?.getFormData());
  }

  return {
    options,
    dataPointValue,
    selectedDocumentMeta,
    extendedDialogRef,
    isPageValid,
    buildApiBodyWithExtendedInfo,
  };
}
