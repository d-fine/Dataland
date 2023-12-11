import { type FrontendFrameworkDefinition } from "@/frameworks/FrameworkDefinition";
import gdv from "@/frameworks/gdv/FrontendFrameworkDefinition";

export const FrameworkDefinitions: Record<string, FrontendFrameworkDefinition<object>> = {
    gdv: gdv,
};
