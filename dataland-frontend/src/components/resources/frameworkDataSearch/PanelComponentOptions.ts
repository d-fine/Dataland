import { DataMetaInformation } from "@clients/backend";

export const PanelProps = {
  companyId: {
    type: String,
  },
  singleDataMetaInfoToDisplay: {
    type: Object as () => DataMetaInformation,
  },
};
