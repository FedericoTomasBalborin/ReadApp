import { createNewBook, updateBook } from "../api/services/BookService"

export const createFormContent = {
    pageTitle: "Crear un nuevo libro",
    pageDescription: "Crea y sube tu libro para permitirle a otros lectores reservarlo.",
    submitButtonLabel: "Crear Libro",
    successMessage: "Libro creado correctamente.",
    errorMessage: "No se pudo crear el libro.",
    shouldLoadBook: false,
    allowTypeEdition: true,
    layout: "create",
    saveBook: async ({ bookObj }) => createNewBook(bookObj)
}

export const updateFormContent = {
    pageTitle: "Editar detalles del libro",
    pageDescription: "Actualiza la información de tu libro listado para ayudar a los lectores a encontrarlo.",
    submitButtonLabel: "Guardar Cambios",
    successMessage: "Libro actualizado correctamente.",
    errorMessage: "No se pudo actualizar el libro.",
    shouldLoadBook: true,
    allowTypeEdition: false,
    layout: "update",
    saveBook: async ({ idBook, bookObj }) => updateBook(idBook, bookObj)
}