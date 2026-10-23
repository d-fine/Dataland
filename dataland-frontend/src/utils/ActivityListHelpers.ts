import { activityTree, type ActivityNode } from '@/components/forms/parts/elements/derived/ActivityTree';

export type ActivityField = { path: string; header: string };

export type ActivityRow = {
  activityName?: string;
  naceCodes?: string[] | null;
  share?: {
    relativeShareInPercent?: number | null;
    absoluteShare?: { amount?: number | null; currency?: string | null } | null;
  } | null;
  relativeEligibleShareInPercent?: number | null;
  enablingActivity?: string | null;
  transitionalActivity?: string | null;
  [key: string]: unknown;
};

/**
 * Finds the NACE codes allowed for an activity.
 * @param activityName the api name of the activity
 * @returns the allowed NACE codes (empty if unknown)
 */
export function getNaceCodesForActivity(activityName: string | undefined | null): string[] {
  if (!activityName) return [];
  const stack: ActivityNode[] = [...activityTree];
  while (stack.length > 0) {
    const node = stack.pop() as ActivityNode;
    if (node.value === activityName) return node.naceCodes ?? [];
    if (node.children) stack.push(...(node.children as ActivityNode[]));
  }
  return [];
}

/**
 * Sets a (possibly nested) cell of a row. Empty values are stored as null.
 * @param rows the live list of rows
 * @param index the row index
 * @param path dot separated path to the cell
 * @param value the new value
 */
export function updateCell(rows: ActivityRow[], index: number, path: string, value: unknown): void {
  const row = rows[index];
  if (!row) return;
  const keys = path.split('.');
  const normalized = value === undefined || value === '' ? null : value;
  let target = row as Record<string, unknown>;
  for (const key of keys.slice(0, -1)) {
    if (target[key] == null || typeof target[key] !== 'object') target[key] = {};
    target = target[key] as Record<string, unknown>;
  }
  target[keys[keys.length - 1]] = normalized;
}

/**
 * Removes a row from the live list.
 * @param rows the live list of rows
 * @param index the row index
 */
export function deleteRow(rows: ActivityRow[], index: number): void {
  if (index >= 0 && index < rows.length) rows.splice(index, 1);
}

/**
 * Appends a row (empty unless an activity is given).
 * @param rows the live list of rows
 * @param activityName optional api name of the activity
 */
export function addRow(rows: ActivityRow[], activityName?: string): void {
  rows.push({ activityName, naceCodes: [], share: null });
}

/**
 * Changes the activity of a row and keeps only NACE codes valid for the new activity.
 * @param rows the live list of rows
 * @param index the row index
 * @param activityName the new api name of the activity
 */
export function changeActivity(rows: ActivityRow[], index: number, activityName: string | undefined): void {
  const row = rows[index];
  if (!row) return;
  row.activityName = activityName;
  const allowed = getNaceCodesForActivity(activityName);
  row.naceCodes = (row.naceCodes ?? []).filter((code) => allowed.includes(code));
}
