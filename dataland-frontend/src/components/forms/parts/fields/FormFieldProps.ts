import { type DropdownOption } from "@/utils/PremadeDropdownDatasets";
import { deepCopyObject, type ObjectType } from "@/utils/UpdateObjectUtils";
import { type ComponentPropsOptions } from "vue";

export const BaseFormFieldProps = {
  name: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    default: "",
  },
  label: {
    type: String,
    default: "",
  },
  validation: {
    type: String,
    default: "",
  },
  validationLabel: {
    type: String,
  },
  required: {
    type: Boolean,
    default: false,
  },
  inputClass: {
    type: String,
    default: "col-4",
  },
};

export const FormFieldPropsWithPlaceholder = Object.assign(deepCopyObject(BaseFormFieldProps), {
  placeholder: {
    type: String,
    default: "",
  },
}) as Readonly<ComponentPropsOptions>;

export const DateFormFieldProps = Object.assign(deepCopyObject(FormFieldPropsWithPlaceholder as ObjectType), {
  todayAsMax: {
    type: Boolean,
    default: false,
  },
}) as Readonly<ComponentPropsOptions>;

export const DropdownOptionFormFieldProps = Object.assign(deepCopyObject(FormFieldPropsWithPlaceholder as ObjectType), {
  options: {
    type: Array as () => Array<DropdownOption> | undefined,
    required: true,
  },
  placeholder: {
    default: "Please Select",
  },
}) as Readonly<ComponentPropsOptions>;

export const OptionsFormFieldProps = Object.assign(deepCopyObject(FormFieldPropsWithPlaceholder as ObjectType), {
  options: {
    type: Object,
    required: true,
  },
}) as Readonly<ComponentPropsOptions>;

export const MultiSelectFormProps = Object.assign(deepCopyObject(FormFieldPropsWithPlaceholder as ObjectType), {
  options: {
    type: Array as () => Array<DropdownOption>,
    required: true,
  },
  optionValue: {
    type: [String, Function],
    required: false,
  },
  optionLabel: {
    type: [String, Function],
    required: false,
  },
}) as Readonly<ComponentPropsOptions>;
