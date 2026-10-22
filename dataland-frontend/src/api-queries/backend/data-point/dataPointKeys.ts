export const dataPointKeys = {
  all: ['dataPoint'] as const,
  byDataPointId: (dataPointId: string | undefined) => [...dataPointKeys.all, 'byDataPointId', dataPointId] as const,
};
