export class Data {
    constructor() {
        this.clearAll()
        this.id = null;
        this.name = null;
    }

    clearAll() {
        this.allResult = null
        this.filteredResult = null
    }

    getAllResult(results) {
        this.clearAll()
        this.allResult = results
    }

    getFilteredResult(results) {
        this.clearAll()
        this.filteredResult = results
    }

}