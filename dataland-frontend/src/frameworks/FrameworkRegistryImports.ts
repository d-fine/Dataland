import { type FrameworkDefinition } from "@/frameworks/FrameworkDefinition";
import heimathafen from "@/frameworks/heimathafen";

export const FrameworkDefinitions: Record<string, FrameworkDefinition<object>> = {
    "heimathafen": heimathafen,
};
