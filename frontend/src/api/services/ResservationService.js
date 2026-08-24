import { api } from "../apiClient"
import { URL_SERVIDOR_REST } from "../../utils/configuration"

export const createReservation = async (
    idBook,
    startDate,
    endDate
) => {
    return await api.post(`${URL_SERVIDOR_REST}/api/reservation/create`,
        {
            idBook,
            startDate,
            endDate
        })
}

export const getBibliokarma = async (
    idBook,
    reservationStartDate,
    reservationEndDate
) => {
    return await api.get(`${URL_SERVIDOR_REST}/api/reservation/bibliokarma`, {
        params: {
            idBook,
            reservationStartDate,
            reservationEndDate
        }
    })
}

export const getOwnedBooksReservations = async (
    page,
    size,
    filter
) => {
    return await api.get(`${URL_SERVIDOR_REST}/api/reservation/ownedBooksReservations`, {
        params: {
            page,
            size,
            filter
        }
    })
}

export const getMyReservations = async (
    page,
    size,
    filter
) => {
    return await api.get(`${URL_SERVIDOR_REST}/api/reservation/myReservations`, {
        params: {
            page,
            size,
            filter
        }
    })
} 