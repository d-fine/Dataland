/**
 * Safely converts an arbitrary value into a display-friendly string.
 * Objects are JSON-serialized instead of using their default `[object Object]` representation.
 *
 * @param value - Value to convert.
 * @returns A string representation suitable for form fields.
 */
export function toSafeDisplayString(value: unknown): string {
  if (value === null || value === undefined) {
    return '';
  }

  const valueType = typeof value;

  if (valueType === 'string') {
    return value as string;
  }

  if (valueType === 'number') {
    return (value as number).toString();
  }

  if (valueType === 'boolean') {
    return (value as boolean) ? 'true' : 'false';
  }

  if (valueType === 'bigint') {
    return (value as bigint).toString();
  }

  if (valueType === 'symbol') {
    return (value as symbol).toString();
  }

  if (valueType === 'object' || valueType === 'function') {
    try {
      return JSON.stringify(value);
    } catch {
      return '[unserializable value]';
    }
  }

  return '';
}
