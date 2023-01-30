import { DataTypeEnum } from "@clients/backend";

export class DatasetTableInfo {
  constructor(
    readonly companyName: string,
    readonly dataType: DataTypeEnum,
    readonly year: number,
    readonly status: DatasetStatus,
    readonly uploadTimeInSeconds: number,
    readonly companyId: string,
    readonly dataId: string,
  ) {}
}

export class DatasetStatus {
  static readonly Approved = new DatasetStatus("APPROVED", "green");
  static readonly Requested = new DatasetStatus("REQUESTED", "cyan");
  static readonly Pending = new DatasetStatus("PENDING", "orange");
  static readonly Rejected = new DatasetStatus("REJECTED", "red");
  static readonly Draft = new DatasetStatus("DRAFT", "gray");

  private constructor(readonly text: string, readonly color: string) {}
}
