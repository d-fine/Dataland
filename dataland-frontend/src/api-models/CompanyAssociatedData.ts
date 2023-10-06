/**
 * A clone of the generic CompanyAssociatedData interface from the backend.
 */
export interface CompanyAssociatedData<T> {
  companyId: string;
  reportingPeriod: string;
  data: T;
}
