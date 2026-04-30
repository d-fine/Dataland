export const requestKeys = {
  all: ['requestData'] as const,
  byRequestId: (requestId: string | undefined) => ['requestData', requestId] as const,
};
