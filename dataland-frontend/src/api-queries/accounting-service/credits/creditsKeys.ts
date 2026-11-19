export const creditsKeys = {
  all: ['credits'] as const,
  balance: (companyId: string | undefined) => [...creditsKeys.all, 'balance', companyId] as const,
};
