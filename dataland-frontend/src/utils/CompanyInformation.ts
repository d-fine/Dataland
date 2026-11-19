import { type CompanyInformation, IdentifierType } from '@clients/backend';

/**
 * Gets LEI to be displayed.
 * @param companyInformation the information of the company, if already loaded
 * @returns the first LEI of the company or a dash if the company has no LEI
 */
export function getDisplayLei(companyInformation: CompanyInformation | null | undefined): string {
  return companyInformation?.identifiers?.[IdentifierType.Lei]?.[0] ?? '—';
}
