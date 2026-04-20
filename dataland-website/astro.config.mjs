import { defineConfig } from 'astro/config';
import mdx from '@astrojs/mdx';
import vue from '@astrojs/vue';
import sitemap from '@astrojs/sitemap';
import tailwindcss from '@tailwindcss/vite';
import { fileURLToPath } from 'node:url';
import { dirname, resolve } from 'node:path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

export default defineConfig({
  site: 'https://dataland.com',
  output: 'static',
  integrations: [mdx(), vue(), sitemap()],
  vite: {
    plugins: [tailwindcss()],
    resolve: {
      alias: {
        '@shared-footer': resolve(__dirname, '../dataland-sharedElements/footer/src'),
      },
    },
    server: {
      fs: {
        allow: ['..'],
      },
    },
  },
});
