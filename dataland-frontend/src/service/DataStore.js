import axios from "axios";

export class DataStore {
    constructor(baseUrl) {
        this.axios = axios.create({
            baseURL: baseUrl,
            headers: {
                "Content-type": "application/json"
            }
        })
    }


    async getByName(code, name) {
        try {
            const results = await this.axios.get(`/data/skyminder/${code}/${name}`);
            const data = results.data
            console.log(results.status + "-" + results.statusText)
            if (!data.length){
                return null
            }
            return data;
        } catch (err) {
            console.error(err)
            return null;
        }
    }


}

