import { defineConfig } from 'astro/config';
import mdx from '@astrojs/mdx';
import vue from '@astrojs/vue';
import sitemap from '@astrojs/sitemap';
import tailwindcss from '@tailwindcss/vite';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  site: 'https://dataland.com',
  output: 'static',
  integrations: [mdx(), vue(), sitemap()],
  vite: {
    plugins: [tailwindcss()],
    esbuild: {
      tsconfig: path.resolve(__dirname, 'tsconfig.json'),
    },
    resolve: {
      alias: {
        '@shared': path.resolve(__dirname, '../dataland-frontend/src'),
      },
    },
  },
});
