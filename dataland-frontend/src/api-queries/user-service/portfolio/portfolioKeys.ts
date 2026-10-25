export const portfolioKeys = {
  all: ['portfolio'] as const,
  enriched: (portfolioId: string | undefined) => ['portfolio', 'enriched', portfolioId] as const,
  portfolioAccessRights: (portfolioId: string | undefined) =>
    ['portfolio', 'portfolioAccessRights', portfolioId] as const,
};
