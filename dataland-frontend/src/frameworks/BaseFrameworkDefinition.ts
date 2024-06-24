import { type DataTypeEnum } from '@clients/backend';
import { type MLDTConfig } from '@/components/resources/dataTable/MultiLayerDataTableConfiguration';

interface MLDTConfigViewConfiguration<FrameworkDataType> {
  type: 'MultiLayerDataTable';
  configuration: MLDTConfig<FrameworkDataType>;
}

export type FrameworkViewConfiguration<FrameworkDataType> = MLDTConfigViewConfiguration<FrameworkDataType>;

export interface BaseFrameworkDefinition {
  readonly identifier: DataTypeEnum;
  readonly label: string;
  readonly explanation: string;
}

export interface FrontendFrameworkDefinition<FrameworkDataType> extends BaseFrameworkDefinition {
  getFrameworkViewConfiguration(): FrameworkViewConfiguration<FrameworkDataType>;
}
