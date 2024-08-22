import { type Configuration } from '@clients/backend';
import { type PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi';
import { type AxiosInstance } from 'axios';
import { type BaseFrameworkDefinition } from '@/frameworks/BaseFrameworkDefinition';

export interface BasePublicFrameworkDefinition<FrameworkDataType> extends BaseFrameworkDefinition {
  getPublicFrameworkApiClient(
    configuration?: Configuration,
    axiosInstance?: AxiosInstance
  ): PublicFrameworkDataApi<FrameworkDataType>;
}
