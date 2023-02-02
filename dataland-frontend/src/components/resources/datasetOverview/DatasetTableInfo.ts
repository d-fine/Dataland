import { DataTypeEnum } from "@clients/backend";

export class DatasetTableInfo {
  constructor(
    readonly companyName: string,
    readonly dataType: DataTypeEnum,
    readonly uploadTimeInMs: number,
    readonly companyId: string,
    readonly dataId: string
  ) {}
}
