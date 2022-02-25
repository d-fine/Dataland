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
            const res = await this.axios.get("/data");
            return {
                status: res.status + "-" + res.statusText,
                headers: res.headers,
                id: res.data[0].id,
                name: res.data[0].name,
                payload: res.data[0].payload,
            };
        } catch (err) {
            console.error(err)
            return {
                id: null,
                name: null,
                payload: null,
            };
        }
    }

    async getById(id) {
        try {
            const res = await this.axios.get(`/data/${id}`);
            return {
                status: res.status + "-" + res.statusText,
                headers: res.headers,
                id: res.data.id,
                name: res.data.name,
                payload: res.data.payload,
            };
        } catch (err) {
            console.error(err)
            return {
                id: null,
                name: null,
                payload: null,
            };
        }
    }
}

