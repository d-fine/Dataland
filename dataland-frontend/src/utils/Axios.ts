import { type RawAxiosResponseHeaders } from 'axios';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';

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

/**
 * Extracts the file extension from the http response headers
 * @param headers the headers of the get document http response
 * @returns the file type extension of the downloaded file
 */
export function getFileExtensionFromHeaders(headers: RawAxiosResponseHeaders): DownloadableFileExtension {
  const contentDisposition = assertDefined(getHeaderIfItIsASingleString(headers, 'content-disposition')).split('.');
  return contentDisposition[contentDisposition.length - 1] as DownloadableFileExtension;
}

/**
 * Extracts the content type from the http response headers
 * @param headers the headers of the get document http response
 * @returns the mime type of the received document
 */
export function getMimeTypeFromHeaders(headers: RawAxiosResponseHeaders): string {
  return assertDefined(getHeaderIfItIsASingleString(headers, 'content-type'));
}

type DownloadableFileExtension = 'pdf' | 'xlsx' | 'xls' | 'ods';
