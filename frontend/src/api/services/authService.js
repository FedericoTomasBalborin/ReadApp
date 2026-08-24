import { URL_SERVIDOR_REST } from '../../utils/configuration'
import { publicApi } from '../apiClient'

const API_URL = URL_SERVIDOR_REST + "/api/auth"
export const ACCESS_TOKEN_KEY = "token"
export const REFRESH_TOKEN_KEY = "refreshToken"

const decodeJwtPayload = (token) => {
    if (!token || typeof token !== "string") {
        return null
    }

    try {
        const payload = token.split(".")[1]
        if (!payload) return null

        const normalized = payload.replace(/-/g, "+").replace(/_/g, "/")
        const decoded = atob(normalized)
        return JSON.parse(decoded)
    } catch {
        return null
    }
}

export const parseUserFromToken = (token) => {
    const payload = decodeJwtPayload(token)
    
    if (!payload)  return null

    const id = payload.userId
    const roles = payload.roles

    if (!id || !roles) return null
    
    return { id, roles }
}

export const getAccessToken = () => localStorage.getItem(ACCESS_TOKEN_KEY)

export const getRefreshToken = () => localStorage.getItem(REFRESH_TOKEN_KEY)

export const setSessionTokens = ({ token, refreshToken }) => {
    if (token) {
        localStorage.setItem(ACCESS_TOKEN_KEY, token)
    }

    if (refreshToken) {
        localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
    }
}

export const clearSession = () => {
    localStorage.removeItem(ACCESS_TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
}

export const login = async (credentials) => {
    return await publicApi.post(`${API_URL}/login`, credentials)
}

export const refreshAccessToken = async (refreshToken) => {
    return await publicApi.post(`${API_URL}/refresh`, { refreshToken })
}

export const logout = async () => {
    clearSession()
}