import { Outlet } from "react-router-dom"
import Header from "./Header/Header"

const Layout = () => {
    return (
        <div className="flex flex-col w-full h-full items-center bg-gray-100 min-h-screen">
            <Header />
            <Outlet />
        </div>
    )
}

export default Layout