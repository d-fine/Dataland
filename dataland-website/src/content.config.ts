import { defineCollection, z } from 'astro:content';
import { glob } from 'astro/loaders';

const successStories = defineCollection({
  loader: glob({ pattern: '**/*.mdx', base: './src/content/success-stories' }),
  schema: z.object({
    slug: z.string(),
    title: z.string(),
    companyName: z.string(),
    companyType: z.string(),
    logo: z.string(),
    processImage: z.string().optional(),
    quoteText: z.string(),
    quoteAuthor: z.string(),
    quoteRole: z.string(),
  }),
});

export const collections = {
  'success-stories': successStories,
};
