import { type RawAxiosResponseHeaders } from 'axios';

/**
 * Retrieve a header from the headers object if it is a single string
 * @param headers the headers object
 * @param header the header to retrieve
 * @returns the header value if it is a single string, otherwise undefined
 */
export function getHeaderIfItIsASingleString(headers: RawAxiosResponseHeaders, header: string): string | undefined {
  const headerValue = headers[header];
  if (typeof headerValue === 'string') {
    return headerValue;
  }
  return undefined;
}
