export const dataPointKeys = {
  all: ['dataPoint'] as const,
  byDataPointId: (dataPointId: string | undefined) => ['dataPoint', 'byDataPointId', dataPointId] as const,
};
