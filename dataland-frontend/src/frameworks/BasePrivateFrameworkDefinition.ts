import { type Configuration } from '@clients/backend';
import { type PrivateFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi';
import { type AxiosInstance } from 'axios';
import { type BaseFrameworkDefinition } from '@/frameworks/BaseFrameworkDefinition';

export interface BasePrivateFrameworkDefinition<FrameworkDataType> extends BaseFrameworkDefinition {
  getPrivateFrameworkApiClient(
    configuration?: Configuration,
    axiosInstance?: AxiosInstance
  ): PrivateFrameworkDataApi<FrameworkDataType>;
}
