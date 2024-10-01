/**
 * Module to convert single letter NACE code to full sector name
 */

import { type KpiValue } from '@/components/resources/frameworkDataSearch/KpiDataObject';
import { naceCodeMap } from '@/components/forms/parts/elements/derived/NaceCodeTree';

/**
 * convert the NACE code to full sector name
 * @param  {KpiValue} kpiValue is the value that needs to be converted corresponding to its field
 * @returns the full sector name including NACE code at the beginning
 */
export function convertNace(kpiValue: KpiValue): KpiValue {
  return Array.isArray(kpiValue)
    ? kpiValue.map((naceCodeShort: string) => naceCodeMap.get(naceCodeShort)?.label ?? naceCodeShort)
    : (naceCodeMap.get(kpiValue as string)?.label ?? kpiValue);
}

/**
 * Converts a single NACE code to it's human-readable description
 * @param naceCode the raw nace code.
 * @returns a human-readable version of the nace code
 */
export function convertSingleNaceCode(naceCode: string): string {
  return naceCodeMap.get(naceCode)?.label ?? naceCode;
}
