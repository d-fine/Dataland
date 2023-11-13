import { type Configuration, type DataTypeEnum } from "@clients/backend";
import { type MLDTConfig } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import { type FrameworkDataApi } from "@/utils/api/UnifiedFrameworkDataApi";

interface MLDTConfigViewConfiguration<FrameworkDataType> {
  type: "MultiLayerDataTable";
  configuration: MLDTConfig<FrameworkDataType>;
}

export type FrameworkViewConfiguration<FrameworkDataType> = MLDTConfigViewConfiguration<FrameworkDataType>;

export interface FrameworkDefinition<FrameworkDataType> {
  readonly identifier: DataTypeEnum;
  readonly label: string;
  readonly explanation: string;

  getFrameworkViewConfiguration(): FrameworkViewConfiguration<FrameworkDataType>;
  getFrameworkApiClient(configuration: Configuration | undefined): FrameworkDataApi<FrameworkDataType>;
}
