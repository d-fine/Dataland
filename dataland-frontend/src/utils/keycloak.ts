import axios, {AxiosResponse} from "axios";
import Keycloak from "keycloak-js";

const qs = require('qs');

function authentication_config(data: object) {
    return {
        method: 'post',
        url: 'http://localhost:8095/realms/datalandsecurity/protocol/openid-connect/token',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        data: qs.stringify(data)
    }
}

function registeration_config(user_data: object, admin_token: string) {
    return {
        method: 'post',
        url: 'http://localhost:8095/realms/datalandsecurity/users',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + admin_token
        },
        data: qs.stringify(user_data)
    }
}


function keycloack_activate(access_token: string, refresh_token: string) {
    const initOptions = {
        url: 'http://localhost:8095/', realm: 'datalandsecurity', clientId: 'dataland-frontend'
    }
    const keycloak = new Keycloak(initOptions)
    keycloak.init({
        checkLoginIframe: false,
        token: access_token, refreshToken: refresh_token
    }).then((auth) => {
        if (auth) {
            console.log("Authenticated");
            alert("Authenticated")
        } else {
            alert("Not Authenticated")
            console.log("Not Authenticated");

        }
        if (keycloak.token) {
            window.sessionStorage.setItem('keycloakToken', keycloak.token)
        }
//Token Refresh
        setInterval(() => {
            keycloak.updateToken(70).then((refreshed) => {
                if (refreshed) {
                    console.log('Token refreshed' + refreshed);
                } else {
                    console.warn('Token not refreshed, valid for ')
                }
            }).catch(() => {
                console.error('Failed to refresh token');
            });
        }, 6000)

    }).catch(() => {
        console.error("Authenticated Failed");
    });
}


export function authenticate(email: string, password: string) {
    const data = qs.stringify({
        'grant_type': 'password',
        'client_id': 'dataland-frontend',
        'username': email,
        'password': password
    });
    const config = {
        method: 'post',
        url: 'http://localhost:8095/realms/datalandsecurity/protocol/openid-connect/token',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        data: data
    };
    axios(config)
        .then(function (response: AxiosResponse) {
            console.log(JSON.stringify(response.data));
            keycloack_activate(response.data.access_token, response.data.refresh_token)
        })
        .catch(function (error: Error) {
            console.log(error);
        })
}

function retrieve_admin_token() {
    console.log("retrieve_admin_token")
    const data = {
        'grant_type': 'client_credentials',
        'client_id': 'dataland-frontend',
        'client_secret': 'bZJhNnmW1Of6GAZGF1hwjGY8mw629McW'
    };
    return axios(authentication_config(data))
}

export function register() {
    console.log("register")
    const user_data = {
        "firstName": "Sergey",
        "lastName": "Kargopolov",
        "email": "test@test.com",
        "password": "123456",
        "enabled": "true",
        "username": "app-user"
    }
    retrieve_admin_token().then((response) => {
        return axios(registeration_config(user_data, response.data.access_token))
    })
        .then((response: AxiosResponse) => {
            console.log(JSON.stringify(response.data));
        })
        .catch(function (error: Error) {
            console.log(error);
        })
}

