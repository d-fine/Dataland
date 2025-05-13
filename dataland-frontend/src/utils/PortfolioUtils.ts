import type { EnrichedPortfolioEntry } from '@clients/userservice';

/**
 * Constant list of major frameworks used across the application
 */
export const MAJOR_FRAMEWORKS = ['sfdr', 'eutaxonomy-financials', 'eutaxonomy-non-financials', 'nuclear-and-gas'];

/**
 * Interface for representing reporting periods with toggle functionality
 */
export interface ReportingPeriod {
  name: string;
  value: boolean;
}

/**
 * Extracts available reporting periods for a specific framework from a portfolio entry
 * and returns them as a formatted string
 *
 * @param portfolioEntry An EnrichedPortfolioEntry object or another object with available reporting periods
 * @param frameworkName The name of the framework for which to extract reporting periods
 * @returns A formatted string with available reporting periods or "No data available"
 */
export function getAvailableReportingPeriodsAsString(
  portfolioEntry: EnrichedPortfolioEntry | { availableReportingPeriods?: Record<string, string | undefined> },
  frameworkName: string
): string {
  if (!portfolioEntry.availableReportingPeriods) {
    return 'No data available';
  }

  return portfolioEntry.availableReportingPeriods[frameworkName] || 'No data available';
}

/**
 * Extracts available reporting periods for a specific framework from portfolio entries
 * and returns them as an array of strings
 *
 * @param portfolioEntries Array of EnrichedPortfolioEntry objects
 * @param frameworkName The name of the framework for which to extract reporting periods
 * @returns An array of strings with available reporting periods
 */
export function getAvailableReportingPeriodsAsArray(
  portfolioEntries: EnrichedPortfolioEntry[],
  frameworkName: string
): string[] {
  if (!portfolioEntries || portfolioEntries.length === 0) {
    return [];
  }

  const availablePeriods = new Set<string>();

  portfolioEntries.forEach((entry) => {
    const periodsString = getAvailableReportingPeriodsAsString(entry, frameworkName);

    if (periodsString !== 'No data available') {
      // Split the string into individual years (typically comma-separated)
      const periods = periodsString.split(',').map((p) => p.trim());
      periods.forEach((period) => {
        if (period) {
          availablePeriods.add(period);
        }
      });
    }
  });

  return Array.from(availablePeriods).sort((a, b) => {
    return parseInt(b) - parseInt(a);
  });
}

/**
 * Converts an array of years to an array of ReportingPeriod objects
 * for use with ToggleChipFormInputs
 *
 * @param years Array of years as strings or numbers
 * @param initialValue Initial value for the value property (default: false)
 * @returns Array of ReportingPeriod objects
 */
export function createReportingPeriodOptions(
  years: (string | number)[],
  initialValue: boolean = false
): ReportingPeriod[] {
  return years.map((year) => ({
    name: year.toString(),
    value: initialValue,
  }));
}

/**
 * Converts a hyphenated string to camelCase
 * e.g., "eutaxonomy-financials" to "eutaxonomyFinancials"
 *
 * @param hyphenatedString A string with hyphens
 * @returns The string in camelCase
 */
export function convertHyphenatedStringToCamelCase(hyphenatedString: string): string {
  return hyphenatedString
    .split('-')
    .map((word, index) => (index === 0 ? word : word.charAt(0).toUpperCase() + word.slice(1)))
    .join('');
}
