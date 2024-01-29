import { type Configuration, type DataTypeEnum } from "@clients/backend";
import { type MLDTConfig } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import { type FrameworkDataApi } from "@/utils/api/UnifiedFrameworkDataApi";
import { type AxiosInstance } from "axios";

interface MLDTConfigViewConfiguration<FrameworkDataType> {
  type: "MultiLayerDataTable";
  configuration: MLDTConfig<FrameworkDataType>;
}

export type FrameworkViewConfiguration<FrameworkDataType> = MLDTConfigViewConfiguration<FrameworkDataType>;

export interface BaseFrameworkDefinition<FrameworkDataType> {
  readonly identifier: DataTypeEnum;
  readonly label: string;
  readonly explanation: string;

  getFrameworkApiClient(
    configuration?: Configuration,
    axiosInstance?: AxiosInstance,
  ): FrameworkDataApi<FrameworkDataType>;
}

export interface FrontendFrameworkDefinition<FrameworkDataType> extends BaseFrameworkDefinition<FrameworkDataType> {
  getFrameworkViewConfiguration(): FrameworkViewConfiguration<FrameworkDataType>;
}
