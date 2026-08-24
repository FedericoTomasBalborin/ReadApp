import { Navigate, Outlet } from "react-router-dom"
import { useAuth } from "../context/AuthContext"
import { getAccessToken, parseUserFromToken } from "../api/services/authService"

const ProtectedRoutes = ({ allowedRoles = [] }) => {
    const { user, setUser } = useAuth()

    if (!user) {
        const token = getAccessToken()
        if (!token) return <Navigate to="/auth" replace />

        const parsedUser = parseUserFromToken(token)
        if (!parsedUser) return <Navigate to="/auth" replace />

        setUser(parsedUser)
        return null
    }

    if (allowedRoles.length === 0 || allowedRoles.some((rol) => user.roles?.includes(rol.name))) {
        return <Outlet />
    }

    return <Navigate to="/" replace />
}

export default ProtectedRoutes