/**
 * Tries to find a message in an error
 * @param error the error to extract a message from
 * @returns the extracted message
 */
export function getErrorMessage(error: unknown): string {
  const noStringMessage = error instanceof Error ? error.message : '';
  return typeof error === 'string' ? error : noStringMessage;
}
