import { Route, Routes } from 'react-router-dom'
import Layout from '../components/Layout/Layout'
import Home from '../components/pages/Home'
import Profile from '../components/pages/Profile'
import BookDetail from '../components/pages/BookDetails'
import MyLoans from '../components/pages/MyLoans'
import BookEdit from '../components/pages/BookEdit'
import KpiDashboard from '../components/pages/KpiDashboard'
import Auth from '../components/pages/Auth'
import ProtectedRoutes from './ProtectedRoutes'
import { createFormContent, updateFormContent } from '../utils/bookEditContent'
import { USER_TYPE } from '../api/domain/USER_TYPE'

const AppRouter = () => {
    return (
        <Routes>

            <Route element={<ProtectedRoutes allowedRoles={
                [USER_TYPE.PUBLISHER, USER_TYPE.READER]
            } />}>
                <Route element={<Layout />}>
                    <Route path={"/"} element={<Home />} />
                    <Route path={"/myLoans"} element={<MyLoans />} />
                    <Route path={"/profile"} element={<Profile />} />
                    <Route path={"/bookDetails/:idBook"} element={<BookDetail />} />
                </Route>
            </Route>

            <Route element={<ProtectedRoutes allowedRoles={
                [USER_TYPE.PUBLISHER]
            } />}>
                <Route element={<Layout />}>
                    <Route path={"/bookEdit"} element={<BookEdit formContent={createFormContent} />} />
                    <Route path={"/bookEdit/:idBook"} element={<BookEdit formContent={updateFormContent} />} />
                </Route>
            </Route>

            <Route element={<ProtectedRoutes allowedRoles={
                [USER_TYPE.ADMIN]
            } />}>
                <Route element={<Layout />}>
                    <Route path={"/kpiDashboard"} element={<KpiDashboard />} />
                </Route>
            </Route>

            <Route path="/auth" element={<Auth />} />
        </Routes>
    )
}

export default AppRouter