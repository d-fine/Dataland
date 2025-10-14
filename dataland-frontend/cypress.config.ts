import { defineConfig } from 'cypress';
import { promises, rmdir } from 'fs';
import { createHash } from 'crypto';
import { readdir } from 'fs/promises';
import { join } from 'path';

let returnEmail: string;
let returnPassword: string;
let returnTotpKey: string;

export default defineConfig({
  env: {
    commit_id: require('git-commit-id')({ cwd: '../' }),
    prepopulate_timeout_s: 180,
    short_timeout_in_ms: 10000,
    medium_timeout_in_ms: 30000,
    long_timeout_in_ms: 100000,
    mobile_device_viewport_height: 667,
    mobile_device_viewport_width: 300,
    AWAIT_PREPOPULATION_RETRIES: 250,
    EXECUTION_ENVIRONMENT: 'developmentLocal',
    KEYCLOAK_DATALAND_ADMIN_PASSWORD: process.env.KEYCLOAK_DATALAND_ADMIN_PASSWORD,
    KEYCLOAK_REVIEWER_PASSWORD: process.env.KEYCLOAK_REVIEWER_PASSWORD,
    KEYCLOAK_PREMIUM_USER_PASSWORD: process.env.KEYCLOAK_PREMIUM_USER_PASSWORD,
    KEYCLOAK_UPLOADER_PASSWORD: process.env.KEYCLOAK_UPLOADER_PASSWORD,
    KEYCLOAK_READER_PASSWORD: process.env.KEYCLOAK_READER_PASSWORD,
    KEYCLOAK_ADMIN_PASSWORD: process.env.KEYCLOAK_ADMIN_PASSWORD,
    KEYCLOAK_ADMIN: process.env.KEYCLOAK_ADMIN,
    PGADMIN_PASSWORD: process.env.PGADMIN_PASSWORD,
    RABBITMQ_PASS: process.env.RABBITMQ_PASS,
    RABBITMQ_USER: process.env.RABBITMQ_USER,
    DATA_REQUEST_UPLOAD_MAX_FILE_SIZE_IN_MEGABYTES:
      process.env.DATA_REQUEST_UPLOAD_MAX_FILE_SIZE_IN_MEGABYTES !== undefined
        ? process.env.DATA_REQUEST_UPLOAD_MAX_FILE_SIZE_IN_MEGABYTES
        : '2',
    GRAFANA_ADMIN: process.env.GRAFANA_ADMIN,
    GRAFANA_PASSWORD: process.env.GRAFANA_PASSWORD,
  },
  experimentalMemoryManagement: true,
  numTestsKeptInMemory: 1,
  defaultCommandTimeout: 300000,
  pageLoadTimeout: 120000,
  viewportHeight: 684,
  viewportWidth: 1536,
  video: false,

  retries: {
    runMode: 2,
    openMode: 1,
  },
  watchForFileChanges: false,

  fixturesFolder: '../testing/data',
  downloadsFolder: './tests/e2e/cypress_downloads',

  e2e: {
    baseUrl: 'https://local-dev.dataland.com',
    setupNodeEvents(on, config) {
      const executionEnvironment = config.env['EXECUTION_ENVIRONMENT'];

      console.log(`Execution environment: ${executionEnvironment}`);
      if (executionEnvironment === 'developmentLocal') {
        console.log(
          'Detected local development run. Running all tests per default. In order to run a specific test run npm run cypress run --spec <./.../specific_test.ts>'
        );
        config.specPattern = ['tests/e2e/specs'];
        config.defaultCommandTimeout = 22000;
      } else {
        console.log('Detected preview / development CI environment. Only loading index.ts to run all tests');
        config.specPattern = ['tests/e2e/specs/index.ts'];
      }
      require('@cypress/code-coverage/task')(on, config);

      on('task', {
        setEmail: (val: string) => {
          return (returnEmail = val);
        },
        getEmail: () => {
          return returnEmail;
        },
      });
      on('task', {
        setPassword: (val: string) => {
          return (returnPassword = val);
        },
        getPassword: () => {
          return returnPassword;
        },
      });
      on('task', {
        setTotpKey: (val: string) => {
          return (returnTotpKey = val);
        },
        getTotpKey: () => {
          return returnTotpKey;
        },
      });
      on('task', {
        deleteFolder(folderName) {
          return new Promise((resolve, reject) => {
            rmdir(folderName, { recursive: true }, (err) => {
              if (err) {
                console.error(err);
                return reject(err);
              }
              resolve(null);
            });
          });
        },
      });

      on('task', {
        async readdir(path: string) {
          return await promises.readdir(path);
        },
      });

      on('task', {
        async readFile(path: string): Promise<Buffer> {
          return await promises.readFile(path);
        },
      });

      on('task', {
        calculateHash(file: Buffer): string {
          return createHash('sha256').update(file).digest('hex');
        },
      });

      on('task', {
        async getFileSize(path: string) {
          const stats = await promises.stat(path);
          return stats.size;
        },
      });

      on('task', {
        deleteFile(path: string) {
          return promises.unlink(path).then(() => null);
        },
      });

      on('task', {
        async checkFileContent({ path, term }) {
          const content = await promises.readFile(path, 'utf8');
          return content.includes(term);
        },
      });
      on('task', {
        async findFileByPrefix({ folder, prefix, extension }) {
          const files = await readdir(folder);
          const match = files.find((file) => file.startsWith(prefix) && file.endsWith(`.${extension}`));
          if (!match) {
            throw new Error(`No file found starting with '${prefix}' and ending with '.${extension}'`);
          }
          return join(folder, match);
        },
      });
      on('task', {
        createUniquePdfFixture() {
          const fs = require('fs');
          const path = require('path');
          const timestamp = Date.now();
          const pdfContent = `Test file created at ${timestamp}`;
          const destDir = path.resolve(__dirname, 'tests/e2e/fixtures/documents');
          if (!fs.existsSync(destDir)) {
            fs.mkdirSync(destDir, { recursive: true });
          }
          const filename = `upload-${timestamp}.pdf`;
          const destFile = path.join(destDir, filename);
          // Minimal PDF file generation (single page, text only)
          const header = Buffer.from('%PDF-1.1\n');
          const obj1 = Buffer.from('1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n');
          const obj2 = Buffer.from('2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n');
          const obj3 = Buffer.from(
            '3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 300 144] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n'
          );
          const text = pdfContent.replace(/([()\\])/g, '\\$1');
          const stream = Buffer.from(`BT /F1 24 Tf 50 100 Td (${text}) Tj ET`);
          const obj4_start = Buffer.from(`4 0 obj\n<< /Length ${stream.length} >>\nstream\n`);
          const obj4_end = Buffer.from('\nendstream\nendobj\n');
          const obj4 = Buffer.concat([obj4_start, stream, obj4_end]);
          const obj5 = Buffer.from('5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n');
          const xrefOffset = header.length + obj1.length + obj2.length + obj3.length + obj4.length + obj5.length;
          const xref = Buffer.from(
            `xref\n0 6\n0000000000 65535 f \n${String(header.length).padStart(10, '0')} 00000 n \n${String(header.length + obj1.length).padStart(10, '0')} 00000 n \n${String(header.length + obj1.length + obj2.length).padStart(10, '0')} 00000 n \n${String(header.length + obj1.length + obj2.length + obj3.length).padStart(10, '0')} 00000 n \n${String(header.length + obj1.length + obj2.length + obj3.length + obj4.length).padStart(10, '0')} 00000 n \n`
          );
          const trailer = Buffer.from('trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n' + xrefOffset + '\n%%EOF\n');
          const pdfBuffer = Buffer.concat([header, obj1, obj2, obj3, obj4, obj5, xref, trailer]);
          fs.writeFileSync(destFile, pdfBuffer);
          return filename;
        },
      });

      return config;
    },
    supportFile: 'tests/e2e/support/index.ts',
    downloadsFolder: 'cypress/downloads',
    responseTimeout: 300000,
    requestTimeout: 300000,
  },
  component: {
    devServer: {
      framework: 'vue',
      bundler: 'vite',
    },
    specPattern: ['tests/component/**/*.cy.ts'],
    supportFile: 'tests/component/component.ts',
    indexHtmlFile: 'tests/component/component-index.html',
    setupNodeEvents(on, config) {
      require('@cypress/code-coverage/task')(on, config);
      return config;
    },
  },
});
