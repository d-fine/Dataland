import { computed, ref, type ComputedRef, type Ref } from 'vue';
import type { DocumentMetaInfoResponse } from '@clients/documentmanager';
import {
  buildApiBody,
  parseValue,
  type ExtendedDataPointType,
  type ExtendedDataPointMetaInfoType,
} from '@/components/resources/dataTable/conversion/Utils.ts';

export type ExtendedDialogRef = Ref<
  { getFormData: () => ExtendedDataPointMetaInfoType; isPageValid: boolean } | undefined
>;

export type NumericExtendedDataPointFormFieldApi = {
  dataPointValue: Ref<number | null>;
  selectedDocumentMeta: Ref<DocumentMetaInfoResponse | undefined>;
  extendedDialogRef: ExtendedDialogRef;
  isPageValid: ComputedRef<boolean>;
  buildApiBodyWithExtendedInfo: () => string;
};

/**
 * Shared reactive state and helpers for extended data point form fields whose value is a plain
 * number without a currency (e.g. big decimal, percentage). Extracted to avoid duplicating the
 * identical script logic across those form field components.
 * @param extendedDataPointObject the extended data point object to derive the initial value from
 * @returns the reactive state and helper functions required by the form field components
 */
export function useNumericExtendedDataPointFormField(
  extendedDataPointObject: ExtendedDataPointType
): NumericExtendedDataPointFormFieldApi {
  const dataPointValue = ref<number | null>(parseValue(extendedDataPointObject.value));
  const selectedDocumentMeta = ref<DocumentMetaInfoResponse | undefined>(undefined);
  const extendedDialogRef: ExtendedDialogRef = ref();

  /**
   * Whether the page reference entered in the extended dialog is valid.
   */
  const isPageValid = computed<boolean>(() => extendedDialogRef.value?.isPageValid ?? true);

  /**
   * Builds the API body for the data point, including the extended meta info from the dialog.
   * @returns the JSON-stringified API body
   */
  function buildApiBodyWithExtendedInfo(): string {
    return buildApiBody(dataPointValue.value, undefined, extendedDialogRef.value?.getFormData());
  }

  return { dataPointValue, selectedDocumentMeta, extendedDialogRef, isPageValid, buildApiBodyWithExtendedInfo };
}
