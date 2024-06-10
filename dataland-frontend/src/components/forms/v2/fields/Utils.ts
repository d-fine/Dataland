import { type FormKitFrameworkContext } from "@formkit/core";

export type FormFieldContext<DataType, Props> = FormKitFrameworkContext<DataType> & Props;
