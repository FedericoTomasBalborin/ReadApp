import { api } from "../apiClient"
import { URL_SERVIDOR_REST } from "../../utils/configuration"

export const getMyBooks = async (page, filterBy, orderBy, isASC) => {
    const response = await api.get(`${URL_SERVIDOR_REST}/books/mybooks`, {
        params: {
            page,
            filterBy,
            orderBy,
            isASC
        }
    })
    return response.data
}

export const getBookDetailById = async (idBook, startDate, endDate) => {
    return await api.get(`${URL_SERVIDOR_REST}/books/book-detail/${idBook}`,{
        params: {
            startDate,
            endDate
        }
    })
}

export const deleteBook = async (idBook) => {
    return await api.delete(`${URL_SERVIDOR_REST}/books/${idBook}`)
}

export const recoverDeletedBook = async (idBook) => {
    return await api.put(`${URL_SERVIDOR_REST}/books/${idBook}/restore`)
}

export const filterBooks = async (data, page, size) => {
    return await api.post(`${URL_SERVIDOR_REST}/books/filtered`, 
        data,
        {params: { page, size }}
    )
}

export const getBookEditableFields = async (idBook) => {
    return await api.get(`${URL_SERVIDOR_REST}/books/form/${idBook}`)
}

export const createNewBook = async (bookObj) => {
    return await api.post(`${URL_SERVIDOR_REST}/books/create`, bookObj)
}

export const updateBook = async (idBook, bookObj) => {
    return await api.put(`${URL_SERVIDOR_REST}/books/update/${idBook}`, bookObj)
}