import { type FrameworkDefinition } from "@/frameworks/FrameworkDefinition";
import gdv from "@/frameworks/gdv";

export const FrameworkDefinitions: Record<string, FrameworkDefinition<object>> = {
  gdv: gdv,
};
