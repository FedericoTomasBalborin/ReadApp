import useAlert from "./useAlert"

const errorCheck = (error) => {
    if (error.response?.status >= 400 && error.response?.status <= 499) {
        return {
            message: error.response.data.message 
        }
    } else {
        return {
            message: "Error interno del sistema"
        }
    }
}

const errorHandler = (error) => {
    const { showError } = useAlert()

    const {message} = errorCheck(error)
    showError(message)
}

export default errorHandler;