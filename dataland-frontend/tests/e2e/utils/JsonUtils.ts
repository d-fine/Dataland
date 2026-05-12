/**
 * Safely parses a JSON string and extracts the 'value' property if it exists.
 * If the value is an object, it is stringified via JSON to avoid [object Object].
 *
 * @param raw The raw JSON string to be parsed.
 * @returns The extracted value as a string, or the original string if parsing fails.
 */
export function parseJsonValue(raw?: string): string | undefined {
  if (raw == null) return undefined;

  try {
    const parsed: unknown = JSON.parse(raw);

    if (typeof parsed !== 'object' || parsed === null || !('value' in parsed)) {
      return raw;
    }

    const val = (parsed as { value?: unknown }).value;

    if (val == null) {
      return raw;
    }

    if (typeof val === 'string') {
      return val;
    }

    return JSON.stringify(val);
  } catch {
    return raw;
  }
}
