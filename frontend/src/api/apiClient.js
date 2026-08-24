import axios from "axios"
import { URL_SERVIDOR_REST } from "../utils/configuration"
import { refreshAccessToken, setSessionTokens } from "./services/authService"

const baseURL = import.meta.env.VITE_API_URL || URL_SERVIDOR_REST

const baseConfig = {
    baseURL,
    timeout: 10000,
    headers: {
        "Content-Type": "application/json"
    }
}

export const api = axios.create(baseConfig)

export const publicApi = axios.create(baseConfig)

let isRedirectingToAuth = false
let refreshPromise = null

const AUTH_API_URL = "http://localhost:8080/api/auth"

const getStoredTokens = () => ({
    token: localStorage.getItem("token"),
    refreshToken: localStorage.getItem("refreshToken"),
})

const clearStoredSession = () => {
    localStorage.removeItem("token")
    localStorage.removeItem("refreshToken")
}

const redirectToAuth = () => {
    if (!isRedirectingToAuth && window.location.pathname !== "/auth") {
        isRedirectingToAuth = true
        window.location.replace("/auth")
    }
}

api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("token")

        if (token) {
            config.headers = config.headers ?? {}
            config.headers.Authorization = `Bearer ${token}`
        }

        return config
    },
    (error) => Promise.reject(error)
)

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error?.config
        const status = error?.response?.status
        const requestUrl = originalRequest?.url ?? ""
        const isRefreshRequest = requestUrl.includes("/api/auth/refresh")

        if (status !== 401 || !originalRequest || originalRequest._retry || isRefreshRequest) {
            return Promise.reject(error)
        }

        originalRequest._retry = true

        try {
            if (!refreshPromise) {
                refreshPromise = refreshAccessToken(getStoredTokens().refreshToken)
                    .then((response) => {
                        const { token, refreshToken } = response.data
                        setSessionTokens({ token, refreshToken })
                        return token
                    })
                    .finally(() => {
                        refreshPromise = null
                    })
            }

            const newToken = await refreshPromise
            originalRequest.headers = originalRequest.headers ?? {}
            originalRequest.headers.Authorization = `Bearer ${newToken}`

            return api(originalRequest)
        } catch (refreshError) {
            clearStoredSession()
            redirectToAuth()
            return Promise.reject(refreshError)
        }
    }
)

export default api