import { useEffect, useRef, useState } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../../context/AuthContext'
import errorHandler from '../../../hooks/errorHandler';
import { getHeaderData } from '../../../api/services/userService';

const routes = [
    { name: "Inicio", path: "/" },
    { name: "Mis Prestamos", path: "/myLoans" },
    { name: "Perfil", path: "/profile" }
]

const adminRoutes = routes.concat({ name: "Tablero", path: "/kpiDashboard" })


const Header = () => {
    const [open, setOpen] = useState(false)
    const navigate = useNavigate()
    const menuRef = useRef(null)
    const { user, logout, headerRefreshKey } = useAuth()
    const [headerData, setHeaderData] = useState({
        bibliokarma: 0,
        name: "",
        lastname: ""
    })
    const [currentRoutes, setCurrentRoutes] = useState(routes)

    const initials = headerData.name.substring(0, 1).toUpperCase() + headerData.lastname.substring(0, 1).toUpperCase()
    const fullName = headerData.name + " " + headerData.lastname

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (menuRef.current && !menuRef.current.contains(event.target)) {
                setOpen(false)
            }
        }
        document.addEventListener("mousedown", handleClickOutside)
        return () => {
            document.removeEventListener("mousedown", handleClickOutside)
        }
    }, [])

    const handleLogout = async () => {
        logout()
        navigate('/auth', { replace: true })
        setOpen(false)
    }

    const fetchUserData = async () => {
        try {
            const response = await getHeaderData(user.id)
            if (user.roles.includes("Admin")) {
                setCurrentRoutes(adminRoutes)
            }
            setHeaderData(response.data)
        } catch (error) {
            errorHandler(error)
        }
    }

    useEffect(() => {
        if (!user?.id) return
        fetchUserData()
    }, [user?.id, headerRefreshKey])

    return (
        <header className='flex justify-between w-full p-2 sticky top-0 bg-white z-50 border-b border-gray-300'>
            <nav className='flex p-2 items-center gap-10'>
                <div className="text-2xl font-semibold pb-2">
                    BookLibre
                </div>
                <ul className='flex gap-6 w-full'>
                    {currentRoutes.map((route, i) => (
                        <li key={i} className="font-semibold">
                            <NavLink
                                to={route.path}
                                className={({ isActive }) => `transition ${isActive ? "text-blue-600" : "text-gray-800 hover:text-blue-600"}`}
                            >{route.name}</NavLink>
                        </li>
                    ))}
                </ul>
            </nav>
            <div id="me" ref={menuRef} className="flex flex-row gap-3 justify-center items-center relative">

                <div className='justify-center font-semibold text-gray-600'>
                    {headerData.bibliokarma} Bibliokarmas
                </div>

                <div
                    onClick={() => setOpen(!open)}
                    className="w-10 h-10 rounded-full bg-blue-600 text-white flex items-center justify-center font-semibold cursor-pointer select-none hover:scale-110 hover:bg-blue-800 transition-transform duration-200"
                >
                    {initials}
                </div>

                {open && (
                    <div className="absolute right-0 mt-40 w-48 bg-white shadow-[0_10px_40px_rgba(0,0,0,0.25)] rounded-lg p-3 z-[1000]">
                        <div className="text-sm font-medium text-gray-800 mb-3">
                            {fullName}
                        </div>
                        <button
                            className="w-full text-left text-sm text-red-600 hover:bg-red-50 p-2 rounded cursor-pointer"
                            onClick={() => handleLogout()}
                        >
                            Cerrar sesión
                        </button>
                    </div>
                )}
            </div>
        </header>
    )
}

export default Header