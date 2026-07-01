import type { DataPointDisplay } from '@/utils/DataPoint.ts';

export interface DataPointDataTableRefProps {
  dataPointDisplay: DataPointDisplay;
  dataId?: string;
  dataType?: string;
}
