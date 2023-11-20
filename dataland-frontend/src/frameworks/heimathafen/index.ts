import { type FrameworkDefinition, type FrameworkViewConfiguration } from "@/frameworks/FrameworkDefinition";
import { type Configuration, type HeimathafenData } from "@clients/backend";
import { HeimathafenViewConfiguration } from "@/frameworks/heimathafen/ViewConfig";
import { type FrameworkDataApi } from "@/utils/api/UnifiedFrameworkDataApi";
import { HeimathafenApiClient } from "@/frameworks/heimathafen/ApiClient";

const HeimathafenFrameworkDefinition: FrameworkDefinition<HeimathafenData> = {
  identifier: "heimathafen",
  explanation: "Das Heimathafen Framework",
  label: "Heimathafen",
  getFrameworkViewConfiguration(): FrameworkViewConfiguration<HeimathafenData> {
    return {
      type: "MultiLayerDataTable",
      configuration: HeimathafenViewConfiguration,
    };
  },
  getFrameworkApiClient(configuration: Configuration | undefined): FrameworkDataApi<HeimathafenData> {
    return new HeimathafenApiClient(configuration);
  },
};

export default HeimathafenFrameworkDefinition;
