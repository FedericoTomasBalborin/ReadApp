import { api } from "../apiClient"
import { URL_SERVIDOR_REST } from "../../utils/configuration"

export const createRating = async (
    idBook,
    calification,
    comment
) => {
    return await api.post(`${URL_SERVIDOR_REST}/api/rating/create`, 
        {
            idBook,
            calification,
            comment
        }
    )
}

export const getRatingsFromBook = async (idBook) => {
    return api.get(`${URL_SERVIDOR_REST}/api/rating/getRatingByBook/${idBook}`)
}