import { type AxiosInstance } from "axios";
import { type FrameworkDefinition, type FrameworkViewConfiguration } from "@/frameworks/FrameworkDefinition";
import { type Configuration, type GdvData } from "@clients/backend";
import { GdvViewConfiguration } from "@/frameworks/gdv/ViewConfig";
import { type FrameworkDataApi } from "@/utils/api/UnifiedFrameworkDataApi";
import { GdvApiClient } from "@/frameworks/gdv/ApiClient";

const GdvFrameworkDefinition: FrameworkDefinition<GdvData> = {
  identifier: "gdv",
  explanation: "Das GDV/VÖB Framework",
  label: "GDV/VÖB",
  getFrameworkViewConfiguration(): FrameworkViewConfiguration<GdvData> {
    return {
      type: "MultiLayerDataTable",
      configuration: GdvViewConfiguration,
    };
  },
  getFrameworkApiClient(
    configuration?: Configuration,
    axiosInstance?: AxiosInstance,
  ): FrameworkDataApi<GdvData> {
    return new GdvApiClient(configuration, axiosInstance);
  },
};

export default GdvFrameworkDefinition;
