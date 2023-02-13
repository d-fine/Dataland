import { DataMetaInformation, DatasetQualityStatus } from "@clients/backend";

/**
 * Filters meta info and data pairs for "Accepted" status
 *
 * @param metaInfosAndData the meta info and data pairs to filter
 * @returns the filtered meta info and data pairs
 */
export function filterForAcceptedDatasets(metaInfosAndData: DataMetaInformation[]): DataMetaInformation[] {
    return metaInfosAndData.filter(
        metaInfoAndData => metaInfoAndData.qualityStatus == DatasetQualityStatus.Accepted
    );
}