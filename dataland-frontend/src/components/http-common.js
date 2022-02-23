import axios from "axios";
export default axios.create({
    baseURL: "https://tut-nodejs-bezkoder.herokuapp.com/api",
    headers: {
        "Content-type": "application/json"
    }
});
