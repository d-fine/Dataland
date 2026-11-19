export const portfolioKeys = {
  all: ['portfolio'] as const,
  names: () => [...portfolioKeys.all, 'names'] as const,
  sharedNames: () => [...portfolioKeys.all, 'sharedNames'] as const,
  enriched: (portfolioId: string | undefined) => [...portfolioKeys.all, 'enriched', portfolioId] as const,
  accessRights: (portfolioId: string | undefined) => [...portfolioKeys.all, 'accessRights', portfolioId] as const,
};
