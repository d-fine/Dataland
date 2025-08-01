// src/utils/FileDownloadUtils.ts

import { MAIN_FRAMEWORKS_IN_ENUM_CLASS_ORDER } from '@/utils/Constants.ts';
import { type EnrichedPortfolio } from '@clients/userservice';

/**
 * Trigger a file download in the browser with proper handling for different file types
 * @param content - File content as a string, ArrayBuffer, Blob or other Blob-compatible data
 * @param filename - Name of the file to be downloaded
 */
export function forceFileDownload(content: string | ArrayBuffer | Blob | BlobPart, filename: string): void {
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

  let blob;
  if (content instanceof Blob) {
    blob = content;
  } else {
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

/**
 * Map reporting periods to frameworks for download for portfolio
 */
export function groupAllReportingPeriodsByFrameworkForPortfolio(
  enrichedPortfolio: EnrichedPortfolio
): Map<string, string[]> {
  const map = new Map<string, string[]>();

  enrichedPortfolio?.entries.forEach((entry) => {
    MAIN_FRAMEWORKS_IN_ENUM_CLASS_ORDER.forEach((framework) => {
      const frameworkPeriods = entry.availableReportingPeriods[framework];
      if (!frameworkPeriods) return;

      const frameworkPeriodsCleaned = frameworkPeriods.split(',').map((p) => p.trim());

      if (!map.has(framework)) {
        map.set(framework, []);
      }

      const periods = map.get(framework)!;

      for (const period of frameworkPeriodsCleaned) {
        if (period && !periods.includes(period)) {
          periods.push(period);
        }
      }
    });
  });

  return map;
}

/**
 * Map reporting periods to frameworks for download for company
 */
export function groupReportingPeriodsPerFrameworkForCompany(
  data: { metaInfo: { dataType: string; reportingPeriod: string } }[]
): Map<string, string[]> {
  const map = new Map<string, string[]>();

  data.forEach((item) => {
    const framework = item.metaInfo.dataType;
    const period = item.metaInfo.reportingPeriod;

    if (!map.has(framework)) {
      map.set(framework, []);
    }

    const periods = map.get(framework)!;
    if (!periods.includes(period)) {
      periods.push(period);
    }
  });

  return map;
}
