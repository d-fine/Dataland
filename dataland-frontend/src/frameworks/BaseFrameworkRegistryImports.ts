import { type BaseFrameworkDefinition } from "@/frameworks/FrameworkDefinition";
import gdv from "@/frameworks/gdv/BaseFrameworkDefinition";

export const FrameworkDefinitions: Record<string, BaseFrameworkDefinition<object>> = {
    gdv: gdv,
};
