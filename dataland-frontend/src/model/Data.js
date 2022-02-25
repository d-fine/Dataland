export class Data {
    /* The fields of this class and APIClient.vue will be changed */
    constructor() {
        this.clearAll()
    }

    clearAll() {
        this.id = null;
        this.name = null;
        this.allResult = null
        this.filteredResult = null
    }

    getAllResult(results) {
        this.filteredResult = null
        this.allResult = results
    }

    getFilteredResult(results) {
        this.allResult = null
        this.filteredResult = results
    }

}