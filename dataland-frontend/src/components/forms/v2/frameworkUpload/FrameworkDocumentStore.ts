import { ref, type Ref } from "vue";

/**
 * This store contains all files that are related to the framework 
 * (company reports, certificates, etc...)
 */
export interface FrameworkDocumentStore {
    /**
     * All documents that are related to the framework (reactive).
     * This object should NOT be modified externally. Instead, use the provided methods.
     */
    allDocuments: Ref<FrameworkDocument[]>;
    putDocument: (document: FrameworkDocument) => void;
    removeDocumentByHash: (sha256: string) => void;
    clearDocuments: () => void;
}

export function createFrameworkDocumentStore(): FrameworkDocumentStore {
    return {
        allDocuments: ref([]),
        putDocument: function (document: FrameworkDocument) {
            const docIdx = this.allDocuments.value.findIndex((doc) => doc.sha256hash === document.sha256hash);
            if (docIdx < 0) {
                this.allDocuments.value.push(document);
            } else {
                this.allDocuments.value[docIdx] = document;
            }
        },
        removeDocumentByHash: function (sha256: string) {
            const idx = this.allDocuments.value.findIndex((doc) => doc.sha256hash === sha256);
            this.allDocuments.value.splice(idx, 1);
        },
        clearDocuments: function () {
            this.allDocuments.value = [];
        }
    };

}


export type FrameworkDocument = FrameworkDocumentForUpload | FrameworkDocumentFromDataland;

export interface FrameworkDocumentBase {
    fileName: string;
    sha256hash: string;
    isAlreadyUploaded: boolean;
}

export interface FrameworkDocumentForUpload extends FrameworkDocumentBase {
    file: File;
    isAlreadyUploaded: false;
}

export interface FrameworkDocumentFromDataland extends FrameworkDocumentBase {
    isAlreadyUploaded: true;
}