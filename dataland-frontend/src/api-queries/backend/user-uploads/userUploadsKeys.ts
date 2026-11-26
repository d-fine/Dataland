export const userUploadsKeys = {
  all: ['userUploads'] as const,
  dataMetaInformation: (userId: string | undefined) => [...userUploadsKeys.all, 'dataMetaInformation', userId] as const,
};
