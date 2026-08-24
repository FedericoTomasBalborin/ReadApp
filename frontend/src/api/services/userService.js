import { URL_SERVIDOR_REST } from "../../utils/configuration"
import { api, publicApi } from "../apiClient"

const API_URL = URL_SERVIDOR_REST + "/api/user"

export const createUser = async (credencials) => {
    return await publicApi.post(`${API_URL}`, credencials)
}

export const getCurrentUser = async (idUser) => {
    return await api.get(`${API_URL}/${idUser}`)
}

export const updateUserProfile = async (idUser, profileObj) => {
    return await api.put(`${API_URL}/${idUser}`, profileObj)
}

export const getHeaderData = async (idUser) => {
    return await api.get(`${API_URL}/header/${idUser}`)
}

