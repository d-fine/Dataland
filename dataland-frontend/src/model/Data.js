export class Data {
    constructor() {
        this.clearAll()
    }

    clearAll() {
        this.id = null
        this.name = null
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