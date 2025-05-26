// src/utils/FileDownloadUtils.ts

/**
 * Trigger a file download in the browser with proper handling for different file types
 * @param content - File content as a string or Blob-compatible data
 * @param filename - Name of the file to be downloaded
 */
export function forceFileDownload(content: any, filename: string): void {
  // Determine and set the MIME type of the file
  const fileExtension = filename.split('.').pop()?.toLowerCase();
  let mimeType = 'text/plain';
  switch (fileExtension) {
    case 'xlsx':
      mimeType = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
      break;
    case 'csv':
      mimeType = 'text/csv';
      break;
    case 'json':
      mimeType = 'application/json';
      break;
  }

  // Handle excels files correctly as binary data and all other files as text
  let blob;
  if (content instanceof ArrayBuffer) {
    blob = new Blob([content], { type: mimeType });
  }
  else if (typeof content === 'string') {
    blob = new Blob([content], { type: mimeType });
  }
  else {
    console.warn('Unknown content type, attempt as standard handling: ', typeof content);
    blob = new Blob([content], { type: mimeType });
  }

  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', filename);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}