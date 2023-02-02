import { DataTypeEnum } from "@clients/backend";

export class DatasetTableInfo {
  constructor(
    readonly companyName: string,
    readonly dataType: DataTypeEnum,
    readonly year: number,
    readonly companyId: string,
    readonly dataId: string
  ) {}
}
