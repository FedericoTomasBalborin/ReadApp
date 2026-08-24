/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState } from "react"
import { clearSession, login, parseUserFromToken, setSessionTokens } from "../api/services/authService"

const AuthContext = createContext()


export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState()
    const [headerRefreshKey, setHeaderRefreshKey] = useState(0)

    const loginAction = async (credentials) => {
        const response = await login(credentials)

        if (response.status !== 200) {
            return response
        }

        const { token, refreshToken } = response.data

        if (token && refreshToken) {
            setSessionTokens({ token, refreshToken })
        }

        const authenticatedUser = parseUserFromToken(token)
        setUser(authenticatedUser)

        return response
    }

    const logout = () => {
        clearSession()
        setUser(null)
    }

    const updateUser = (newUser) => {
        setUser(newUser)
    }

    const refreshHeaderData = () => {
        setHeaderRefreshKey((prev) => prev + 1)
    }

    return (
        <AuthContext.Provider value={{ user, setUser, updateUser, loginAction, logout, headerRefreshKey, refreshHeaderData }}>
            {children}
        </AuthContext.Provider>
    )
}

export const useAuth = () => useContext(AuthContext)