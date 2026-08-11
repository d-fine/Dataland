import { type DataPointSourceInfo } from '@/utils/DataPoint.ts';

export interface NextDataPointOption {
  label: string;
  dataPointTypeId: string;
  reviewed: boolean;
}

export interface DocumentOption {
  label: string;
  value: string;
  dataSource: DataPointSourceInfo;
}

export interface CustomFormData {
  value: string;
  quality: string;
  document: string;
  pages: string;
  comment: string;
}
