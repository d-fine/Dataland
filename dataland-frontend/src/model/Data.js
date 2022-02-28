export class Data {
    /* The fields of this class and APIClient.vue will be changed */
    constructor() {
        this.clearAll()
    }

    clearAll() {
        this.id = null;
        this.name = null;
        this.code = null;
        this.result = null
    }


    getResult(results) {
        this.result = results
    }

}