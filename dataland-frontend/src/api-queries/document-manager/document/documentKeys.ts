export const documentKeys = {
  all: ['document'] as const,
  listByCompanyId: (companyId: string | undefined) => ['document', 'byCompanyId', companyId] as const,
};
