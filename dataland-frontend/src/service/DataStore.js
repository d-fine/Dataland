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

    async getAll() {
        try {
            const results = await this.axios.get("/data");
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

    async getById(id)
    {
        try {
            const results = await this.axios.get(`/data/${id}`);
            const data = {
                id: results.data.id,
                name: results.data.name,
                payload: results.data.payload
            }
            console.log(results.status + "-" + results.statusText)
            return data;
        } catch (err) {
            console.error(err)
            return null;
        }
    }
}

