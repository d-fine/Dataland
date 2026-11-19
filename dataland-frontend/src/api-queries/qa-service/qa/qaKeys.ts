export const qaKeys = {
  all: ['qa'] as const,
  pendingDatasets: (companyName: string | undefined) => [...qaKeys.all, 'pendingDatasets', companyName] as const,
};
