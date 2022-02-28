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
            const data = res.data
            return {
                status: res.status + "-" + res.statusText,
                headers: res.headers,
                data: data,
            };
        } catch (err) {
            const data = {
                id: null,
                name: null,
                payload: null
            }
            return {
                status: "500-Internal Server Error",
                headers: "",
                data: data,
            };
        }
    }

    async getById(id)
    {
        try {
            const res = await this.axios.get(`/data/${id}`);
            const data = {
                id: res.data.id,
                name: res.data.name,
                payload: res.data.payload
            }
            return {
                status: res.status + "-" + res.statusText,
                headers: res.headers,
                data: data,
            };
        } catch (err) {
            console.error(err)
            const data = {
                id: null,
                name: null,
                payload: null
            }
            return {
                status: "500-Internal Server Error",
                headers: "",
                data: data,
            };
        }
    }
}

