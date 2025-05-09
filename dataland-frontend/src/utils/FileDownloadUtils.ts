// src/utils/FileDownloadUtils.ts

/**
 * Trigger a file download in the browser
 * @param content - File content as a string or Blob-compatible data
 * @param filename - Name of the file to be downloaded
 */
export function forceFileDownload(content: string, filename: string): void {
  const url = window.URL.createObjectURL(new Blob([content]));
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', filename);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}
