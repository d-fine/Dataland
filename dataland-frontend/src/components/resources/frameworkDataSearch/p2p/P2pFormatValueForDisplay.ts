import { type Field } from '@/utils/GenericFrameworkTypes';
import { type KpiValue } from '@/components/resources/frameworkDataSearch/KpiDataObject';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { formatNumberToReadableFormat, formatPercentageNumberAsString } from '@/utils/Formatter';

/**
 * Formats KPI values for display
 * @param field the considered KPI field
 * @param value the value to be formatted
 * @returns the formatted value
 */
export function formatValueForDisplay(field: Field, value: KpiValue): KpiValue {
  if (field.name == 'sector') {
    return (value as string[]).map((sector) => humanizeStringOrNumber(sector));
  }
  if (field.component == 'PercentageFormField') {
    return formatPercentageNumberAsString(value as number);
  }
  if (typeof value === 'number') {
    return formatNumberToReadableFormat(value);
  }
  return value;
}
