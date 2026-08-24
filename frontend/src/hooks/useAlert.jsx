import Swal from "sweetalert2"

const applyStyles = (popup) => {
    const confirmBtn = popup.querySelector(".swal2-confirm")
    const cancelBtn = popup.querySelector(".swal2-cancel")

    if (confirmBtn) {
        confirmBtn.style.backgroundColor = "#1975d1"
        confirmBtn.style.color = "#fff"
        confirmBtn.style.border = "none"
        confirmBtn.style.borderRadius = "8px"
        confirmBtn.style.padding = "10px 18px"
        confirmBtn.style.fontWeight = "600"
        confirmBtn.style.cursor = "pointer"
        confirmBtn.style.transition = "all 0.2s ease"

        confirmBtn.onmouseover = () => {
            confirmBtn.style.backgroundColor = "#155fa8"
        }

        confirmBtn.onmouseout = () => {
            confirmBtn.style.backgroundColor = "#1975d1"
        }

        confirmBtn.onmousedown = () => {
            confirmBtn.style.transform = "scale(0.97)"
        }

        confirmBtn.onmouseup = () => {
            confirmBtn.style.transform = "scale(1)"
        }
    }

    if (cancelBtn) {
        cancelBtn.style.backgroundColor = "#e5e7eb"
        cancelBtn.style.color = "#111827"
        cancelBtn.style.borderRadius = "8px"
        cancelBtn.style.padding = "10px 18px"
        cancelBtn.style.fontWeight = "600"
        cancelBtn.style.cursor = "pointer"
    }
}

const baseConfig = {
    buttonsStyling: false,
    didOpen: applyStyles
}

const useAlert = () => {

    const showSuccess = (message) => {
        Swal.fire({
        ...baseConfig,
        icon: "success",
        title: "Éxito",
        text: message,
        confirmButtonText: "Ok"
        })
    }

    const showError = (message) => {
        Swal.fire({
        ...baseConfig,
        icon: "error",
        title: "Error",
        text: message,
        confirmButtonText: "Entendido"
        })
    }

    const showConfirm = async (message) => {
        const result = await Swal.fire({
        ...baseConfig,
        icon: "question",
        title: "Confirmar",
        text: message,
        showCancelButton: true,
        confirmButtonText: "Sí, dale",
        cancelButtonText: "Cancelar"
        })

        return result.isConfirmed
    }

    return {
        showSuccess,
        showError,
        showConfirm
    }
}

export default useAlert