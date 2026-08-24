import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { Mail, Lock, User, RefreshCw, UserPlus, HandMetal } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { createUser } from '../../api/services/userService'

const loginSchema = yup.object().shape({
    email: yup.string().email('Email inválido').required('El correo es obligatorio'),
    password: yup.string().required('La contraseña es obligatoria'),
})

const signupSchema = yup.object().shape({
    fullName: yup.string().required('El nombre es obligatorio'),
    email: yup.string().email('Email inválido').required('El correo es obligatorio'),
    password: yup.string().required('La contraseña es obligatoria'),
    confirmPassword: yup
        .string()
        .oneOf([yup.ref('password'), null], 'Las contraseñas no coinciden')
        .required('Debes confirmar tu contraseña'),
})

const Auth = () => {
    const [isLogin, setIsLogin] = useState(true)
    const [errorMessage, setErrorMessage] = useState(null)
    const navigate = useNavigate()
    const { loginAction, user } = useAuth()

    const {
        register,
        handleSubmit,
        formState: { errors },
        reset,
    } = useForm({
        resolver: yupResolver(isLogin ? loginSchema : signupSchema),
    })

    const toggleMode = () => {
        reset()
        setErrorMessage(null)
        setIsLogin(!isLogin)
    }

    useEffect(() => {
        const token = localStorage.getItem('token')
        const storedUser = localStorage.getItem('user')

        if (user || (token && storedUser)) {
            navigate('/', { replace: true })
        }
    }, [navigate, user])

    const onSubmit = async (data) => {
        setErrorMessage(null)

        if (!isLogin) {
            const signupResponse = await createUser({
                fullName: data.fullName,
                email: data.email,
                password: data.password,
            })
            
            if (signupResponse.status !== 204) {
                setErrorMessage(signupResponse.data?.message ?? 'Error al crear la cuenta')
                return
            }
        }

        const loginResponse = await loginAction({
            email: data.email,
            password: data.password,
        })
        
        if (loginResponse.status === 200) {
            navigate('/')
        } else {
            setErrorMessage(loginResponse.data?.message ?? 'Error al iniciar sesión')
        }
    }


    return (
        <div className="min-h-screen bg-[#f8fbff] flex items-center justify-center p-4 font-sans">
            <div className="w-full max-w-md bg-white rounded-3xl shadow-sm overflow-hidden border border-gray-100">

                <div className="pt-10 pb-6 px-8 text-center bg-[#f8fbff]">
                    <div className="flex justify-center mb-4">
                        <div className="w-16 h-16 bg-white rounded-full flex items-center justify-center shadow-sm">
                            {isLogin ? (
                                <HandMetal className="text-blue-500 w-8 h-8" />
                            ) : (
                                <UserPlus className="text-blue-500 w-8 h-8" />
                            )}
                        </div>
                    </div>
                    <h1 className="text-2xl font-bold text-[#1a2b3c]">
                        {isLogin ? "BookLibre" : "Crear una cuenta"}
                    </h1>
                    <p className="text-gray-500 text-sm mt-2">
                        {isLogin
                            ? "Por favor ingresa tus datos para continuar leyendo."
                            : "Únete a nuestra comunidad y empieza a leer."}
                    </p>
                </div>

                <div className="p-8">
                    <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
                        {!isLogin && (
                            <div>
                                <label className="block text-sm font-semibold text-[#344767] mb-1">Nombre completo</label>
                                <div className="relative">
                                    <User className="absolute left-3 top-3 text-gray-400 w-5 h-5" />
                                    <input
                                        {...register("fullName")}
                                        type="text"
                                        placeholder="Ej. Juan Pérez"
                                        className={`w-full pl-10 pr-4 py-3 bg-[#f8fbff] border ${errors.fullName ? 'border-red-500' : 'border-gray-200'} rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-200 transition-all text-sm`}
                                    />
                                </div>
                                {errors.fullName && <span className="text-xs text-red-500 mt-1">{errors.fullName.message}</span>}
                            </div>
                        )}

                        <div>
                            <label className="block text-sm font-semibold text-[#344767] mb-1">Correo electrónico</label>
                            <div className="relative">
                                <Mail className="absolute left-3 top-3 text-gray-400 w-5 h-5" />
                                <input
                                    {...register("email")}
                                    type="email"
                                    placeholder="tu@ejemplo.com"
                                    className={`w-full pl-10 pr-4 py-3 bg-[#f8fbff] border ${errors.email ? 'border-red-500' : 'border-gray-200'} rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-200 transition-all text-sm`}
                                />
                            </div>
                            {errors.email && <span className="text-xs text-red-500 mt-1">{errors.email.message}</span>}
                        </div>

                        <div>
                            <label className="block text-sm font-semibold text-[#344767] mb-1">Contraseña</label>
                            <div className="relative">
                                <Lock className="absolute left-3 top-3 text-gray-400 w-5 h-5" />
                                <input
                                    {...register("password")}
                                    type="password"
                                    placeholder="••••••••"
                                    className={`w-full pl-10 pr-4 py-3 bg-[#f8fbff] border ${errors.password ? 'border-red-500' : 'border-gray-200'} rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-200 transition-all text-sm`}
                                />
                            </div>
                            {errors.password && <span className="text-xs text-red-500 mt-1">{errors.password.message}</span>}
                        </div>

                        {!isLogin && (
                            <div>
                                <label className="block text-sm font-semibold text-[#344767] mb-1">Confirmar contraseña</label>
                                <div className="relative">
                                    <RefreshCw className="absolute left-3 top-3 text-gray-400 w-5 h-5" />
                                    <input
                                        {...register("confirmPassword")}
                                        type="password"
                                        placeholder="••••••••"
                                        className={`w-full pl-10 pr-4 py-3 bg-[#f8fbff] border ${errors.confirmPassword ? 'border-red-500' : 'border-gray-200'} rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-200 transition-all text-sm`}
                                    />
                                </div>
                                {errors.confirmPassword && <span className="text-xs text-red-500 mt-1">{errors.confirmPassword.message}</span>}
                            </div>
                        )}

                        {isLogin && (
                            <div className="flex items-center space-x-2">
                                <input type="checkbox" id="remember" className="w-4 h-4 rounded border-gray-300 text-blue-500 focus:ring-blue-500" />
                                <label htmlFor="remember" className="text-sm text-gray-600">Recordarme</label>
                            </div>
                        )}

                        {errorMessage && (
                            <p className="text-sm text-red-500 text-center">{errorMessage}</p>
                        )}

                        <button
                            type="submit"
                            className="w-full bg-blue-500 hover:bg-blue-600 text-white font-bold py-3 rounded-xl shadow-sm transition-all mt-4 cursor-pointer"
                        >
                            {isLogin ? "Ingresar" : "Crear cuenta"}
                        </button>
                    </form>

                    <div className="mt-8 text-center">
                        <p className="text-sm text-gray-500">
                            {isLogin ? "¿Aún no tienes una cuenta?" : "¿Ya tienes una cuenta?"}{" "}
                            <button
                                onClick={toggleMode}
                                className="text-blue-500 font-medium hover:underline cursor-pointer"
                            >
                                {isLogin ? "Regístrate gratis" : "Inicia sesión"}
                            </button>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default Auth