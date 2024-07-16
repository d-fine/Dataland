import { type FrameworkData } from '@/utils/GenericFrameworkTypes';
import { type DataAndMetaInformation } from '@/api-models/DataAndMetaInformation';

export type DataAndMetaInformationViewModel<T extends FrameworkViewModel> = DataAndMetaInformation<T>;

export interface FrameworkViewModel {
  toApiModel(): FrameworkData;
}

/**
 * Create a data view model with identity toApiModel conversion
 * @param input the data meta information object to wrap.
 * @returns a DataAndMetaInformationViewModel with an identity toApiModel function.
 */
export function getViewModelWithIdentityApiModel<T extends FrameworkData>(
  input: DataAndMetaInformation<T>
): DataAndMetaInformationViewModel<T & FrameworkViewModel> {
  return {
    metaInfo: input.metaInfo,
    data: {
      ...input.data,
      toApiModel: () => input.data,
    },
  };
}
