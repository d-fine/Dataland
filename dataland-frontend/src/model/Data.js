/**
 * Implementation of the data class
 */
export class Data {
    /**
     * initializes the field properties
     */
    constructor() {
        this.clearAll()
    }

    /**
     * Sets all field properties to null
     */
    clearAll() {
        this.id = null
        this.name = null
        this.code = null
        this.result = null
    }

    /**
     * Gets the results and sets them
     */
    getResult(results) {
        this.result = results
    }

}