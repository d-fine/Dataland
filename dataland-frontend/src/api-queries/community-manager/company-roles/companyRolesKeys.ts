import { type CompanyRole } from '@clients/communitymanager';

export interface CompanyRoleAssignmentFilter {
  role?: CompanyRole;
  companyId?: string;
  userId?: string;
}

export const companyRolesKeys = {
  all: ['companyRoles'] as const,
  assignments: (filter: CompanyRoleAssignmentFilter) => [...companyRolesKeys.all, 'assignments', filter] as const,
  hasCompanyAtLeastOneOwner: (companyId: string | undefined) =>
    [...companyRolesKeys.all, 'hasCompanyAtLeastOneOwner', companyId] as const,
};
