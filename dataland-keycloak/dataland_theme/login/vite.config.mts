import { fileURLToPath } from 'url';
import { defineConfig } from 'vite';

export default defineConfig({
    base: './',
    build: {
        rollupOptions: {
            input: {
                index: fileURLToPath(new URL('src/index.ts', import.meta.url)),
                terms: fileURLToPath(new URL('src/terms.ts', import.meta.url)),
                passwordStrength: fileURLToPath(new URL('src/passwordStrength.ts', import.meta.url)),
            },
            output: {
                assetFileNames: '[name][extname]',
                dir: 'build/dist/resources',
                entryFileNames: '[name].js',
            },
        },
        assetsInlineLimit: 0,
    },
});
