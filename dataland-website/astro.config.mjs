import { defineConfig } from 'astro/config';
import mdx from '@astrojs/mdx';
import vue from '@astrojs/vue';
import sitemap from '@astrojs/sitemap';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig({
  site: 'https://dataland.com',
  output: 'static',
  integrations: [mdx(), vue(), sitemap()],
  vite: {
    plugins: [tailwindcss()],
    server: {
      proxy: {
        '/api': {
          target: process.env.VITE_API_PROXY_TARGET ?? 'https://local-dev.dataland.com',
          changeOrigin: true,
          secure: false,
        },
      },
    },
  },
});
