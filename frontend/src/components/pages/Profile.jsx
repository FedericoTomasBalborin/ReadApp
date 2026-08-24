import { useEffect, useState } from 'react';
import { Box, Typography, Button, Paper, Avatar, Table, TableBody, TableCell, TableHead, TableRow, Stack, ToggleButton, ToggleButtonGroup, TextField, CircularProgress, Pagination, FormGroup, FormControlLabel, Checkbox } from "@mui/material";
import BookRow from "../ui/BookCard/BookRow";
import { useOnInit } from "../../hooks/useOnInIt";
import { deleteBook, getMyBooks, recoverDeletedBook } from '../../api/services/BookService';
import { useAuth } from '../../context/AuthContext';
import { NavLink } from 'react-router-dom';
import errorHandler from '../../hooks/errorHandler';
import BasicModal from '../ui/Modal/BasicModal';
import { getCurrentUser, updateUserProfile } from '../../api/services/userService';
import useAlert from '../../hooks/useAlert';
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { Controller, useForm } from 'react-hook-form';
import { getRefreshToken, refreshAccessToken, setSessionTokens } from '../../api/services/authService';


const Profile = () => {
    const [books, setBooks] = useState([])
    const [query, setQuery] = useState({
        filterBy: "ALL",
        orderBy: "TITLE",
        page: 1,
        isAsc: true
    })
    const [totalBooks, setTotalBooks] = useState(0)
    const [openEditProfile, setOpenEditProfile] = useState(false)
    const [profileUser, setProfileUser] = useState(null)
    const { user, setUser } = useAuth()

    const fetchUserData = async () => {
        try {
            const response = await getCurrentUser(user.id)
            setProfileUser(response.data)
        } catch (error) {
            errorHandler(error)
        }
    }

    const fetchBooks = async () => {
        try {
            const data = await getMyBooks(query.page, query.filterBy, query.orderBy, query.isAsc)
            setBooks(data.books)
            setTotalBooks(data.totalSize)
        } catch (error) {
            errorHandler(error)
        }
    }

    const handleDeleteButton = async (id, isActive) => {
        try {
            await (isActive ? deleteBook(id) : recoverDeletedBook(id))
            fetchBooks()
        } catch (error) {
            console.error("Error updating book status", error.code)
        }
    }

    const onFilterChange = (_, value) => {
        setQuery(prev => ({
            ...prev,
            filterBy: value,
            page: 1
        }))
    }

    const onOrderChange = (value) => {
        setQuery(prev => ({
            ...prev,
            orderBy: value,
            isAsc: prev.orderBy === value ? !prev.isAsc : true,
            page: 1
        }))
    }

    const onPageChange = (_, value) => {
        setQuery(prev => ({
            ...prev,
            page: value
        }))
    }

    const openModal = () => {
        setOpenEditProfile(true)
    };

    const closeModal = () => {
        setOpenEditProfile(false);
    };

    useEffect(() => {
        if(user?.roles.includes("Publicador")){
            fetchBooks()
        }
    }, [query.page, query.filterBy, query.orderBy, query.isAsc])

    useOnInit(() => {
        fetchUserData()
    })

    return (
        <Box sx={{ width: "100%", display: "flex", flexDirection: "column" }}>
            <ProfileHeader user={profileUser} openModal={openModal} />
            <Box component="main" sx={{ display: "flex", flex: 1 }}>
                <ProfileAside user={profileUser} />
                <ProfileSection
                    user={user}
                    books={books}
                    totalBooks={totalBooks}
                    handleDeleteButton={handleDeleteButton}
                    query={query}
                    onFilterChange={onFilterChange}
                    onSortChange={onOrderChange}
                    onPageChange={onPageChange}
                />
            </Box>

            <EditProfileModal
                open={openEditProfile}
                handleClose={closeModal}
                user={user}
                setUser={setUser}
                userData={profileUser}
                onProfileUpdated={setProfileUser}
            />
        </Box>
    );
};

const ProfileHeader = ({ openModal, user }) => {
    return (
        <Box>
            <Box
                sx={{
                    maxWidth: "1200px",
                    mx: "auto",
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    p: 3,
                }}
            >
                <Box sx={{ display: "flex", alignItems: "center", gap: 3 }}>
                    <Avatar
                        src="logo.png"
                        alt="user-logo"
                        sx={{
                            width: { xs: 80, md: 112 },
                            height: { xs: 80, md: 112 },
                            bgcolor: "black",
                        }}
                    />

                    <Box>
                        <Box sx={{ mb: 2 }}>
                            <Typography variant="h5" fontWeight="bold">
                                {user?.name} {user?.lastname}
                            </Typography>
                            <Typography variant="body1" color="text.secondary">
                                {user?.description}
                            </Typography>
                        </Box>

                        <Stack direction="row" spacing={3} flexWrap="wrap">
                            <Typography variant="body2" color="text.secondary">
                                Se unió el {user?.createdAt}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                {user?.residenceCity}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                Bibliokarma {user?.bibliokarma}
                            </Typography>
                        </Stack>
                    </Box>
                </Box>

                <Button variant="contained" onClick={() => openModal()}>Editar Perfil</Button>
            </Box>
        </Box>
    );
};

const EditProfileModal = ({ open, handleClose, user, setUser, userData, onProfileUpdated }) => {
    const [isSaving, setIsSaving] = useState(false)
    const { showSuccess } = useAlert()

    const schema = yup.object({
        name: yup.string().required("El nombre es obligatorio"),
        lastname: yup.string().required("El apellido es obligatorio"),
        description: yup.string().required("La descripción es obligatoria"),
        email: yup.string().email("Email inválido").required("El email es obligatorio"),
        phone: yup
            .string()
            .required("El teléfono es obligatorio")
            .matches(/^[0-9]+$/, "Solo números"),
        residenceCity: yup.string().required("La ciudad es obligatoria"),
        roles: yup.object({
            lector: yup.boolean(),
            publicador: yup.boolean()
        }).test(
            "at-least-one",
            "Seleccioná al menos un rol",
            (value) => value?.lector || value?.publicador
        )
    })
    
    const { control, handleSubmit, formState: { errors }, reset, watch } = useForm({
        resolver: yupResolver(schema),
        defaultValues: {
            name: userData?.name ?? "",
            lastname: userData?.lastname ?? "",
            description: userData?.description ?? "",
            email: userData?.email ?? "",
            phone: userData?.phone ?? "",
            residenceCity: userData?.residenceCity ?? "",
            roles: {
                lector: userData?.type.includes("Lector"),
                publicador: userData?.type.includes("Publicador")
            }
        }
    })

    useEffect(() => {
        reset({
            name: userData?.name ?? "",
            lastname: userData?.lastname ?? "",
            description: userData?.description ?? "",
            email: userData?.email ?? "",
            phone: userData?.phone ?? "",
            residenceCity: userData?.residenceCity ?? "",
            roles: {
                lector: userData?.type.includes("Lector"),
                publicador: userData?.type.includes("Publicador")
            }
        })
    }, [userData, reset, open])

    const roles = watch("roles")

    const submit = async (data) => {
        if (!user?.id) return

        const profileObj = {
            name: data.name,
            lastname: data.lastname,
            description: data.description,
            email: data.email,
            phone: data.phone,
            residenceCity: data.residenceCity,
            roles: [
                data.roles.lector && "Lector",
                data.roles.publicador && "Publicador"
            ].filter(Boolean)
        }

        try {
            setIsSaving(true)
            await updateUserProfile(user?.id, profileObj)

            const updatedUser = await getCurrentUser(user?.id)
            const updatedUserData = {
                ...(updatedUser.data ?? {}),
                roles: profileObj.roles
            }

            onProfileUpdated(updatedUserData)
            setUser((prevUser) => ({
                ...(prevUser ?? {}),
                ...updatedUserData
            }))
            
            try{
                const response = await refreshAccessToken(getRefreshToken())
                setSessionTokens(response.data)
            }catch(e){
                errorHandler(e)
            }

            handleClose()
            showSuccess("Perfil actualizado correctamente")
        } catch (error) {
            handleClose()
            errorHandler(error)
        } finally {
            setIsSaving(false)
        }
    }

   return (
        <BasicModal open={open} handleClose={handleClose}>
            <h2 className="text-2xl font-bold mb-6">Editar Perfil</h2>

            <Box component="form" onSubmit={handleSubmit(submit)} className="flex flex-col gap-4">
                <Controller
                    name="name"
                    control={control}
                    render={({ field }) => (
                        <TextField {...field} label="Nombre" size="small" fullWidth
                            error={!!errors.name}
                            helperText={errors.name?.message}
                        />
                    )}
                />

                <Controller
                    name="lastname"
                    control={control}
                    render={({ field }) => (
                        <TextField {...field} label="Apellido" size="small" fullWidth
                            error={!!errors.lastname}
                            helperText={errors.lastname?.message}
                        />
                    )}
                />

                <Controller
                    name="description"
                    control={control}
                    render={({ field }) => (
                        <TextField {...field} label="Descripción" size="small" fullWidth multiline minRows={3} 
                            error={!!errors.description}
                            helperText={errors.description?.message}
                        />
                    )}
                />

                <Controller
                    name="email"
                    control={control}
                    render={({ field }) => (
                        <TextField {...field} type="email" label="Email" size="small" fullWidth
                            error={!!errors.email}
                            helperText={errors.email?.message}
                        />
                    )}
                />

                <Controller
                    name="phone"
                    control={control}
                    render={({ field }) => (
                        <TextField {...field} label="Teléfono" size="small" fullWidth
                            error={!!errors.phone}
                            helperText={errors.phone?.message}
                        />
                    )}
                />

                <Controller
                    name="residenceCity"
                    control={control}
                    render={({ field }) => (
                        <TextField {...field} label="Ciudad" size="small" fullWidth
                            error={!!errors.residenceCity}
                            helperText={errors.residenceCity?.message}
                        />
                    )}
                />

                <FormGroup row>
                    <Controller
                        name="roles.lector"
                        control={control}
                        render={({ field }) => (
                            <FormControlLabel
                                control={
                                    <Checkbox
                                        checked={field.value}
                                        onChange={(e) => {
                                            if (!e.target.checked && !roles.publicador) return
                                            field.onChange(e.target.checked)
                                        }}
                                    />
                                }
                                label="Lector"
                            />
                        )}
                    />

                    <Controller
                        name="roles.publicador"
                        control={control}
                        render={({ field }) => (
                            <FormControlLabel
                                control={
                                    <Checkbox
                                        checked={field.value}
                                        onChange={(e) => {
                                            if (!e.target.checked && !roles.lector) return
                                            field.onChange(e.target.checked)
                                        }}
                                    />
                                }
                                label="Publicador"
                            />
                        )}
                    />
                </FormGroup>

                {errors.roles && (
                    <span style={{ color: "red", fontSize: 12 }}>
                        {errors.roles.message}
                    </span>
                )}

                <Stack direction="row" spacing={2} justifyContent="flex-end">
                    <Button variant="outlined" onClick={handleClose} disabled={isSaving}>
                        Cancelar
                    </Button>

                    <Button type="submit" variant="contained" disabled={isSaving}>
                        {isSaving ? <CircularProgress size={20} color="inherit" /> : "Guardar"}
                    </Button>
                </Stack>
            </Box>
        </BasicModal>
    )
}

const ProfileAside = ({ user }) => {
    return (
        <Box className="p-4">
            <Paper className="p-4">
                <Typography variant="h6" fontWeight="bold" mb={2}>
                    Mis datos
                </Typography>

                <Stack spacing={3}>
                    <Box>
                        <Typography variant="caption" fontWeight="bold" color="text.secondary">
                            EMAIL
                        </Typography>
                        <Typography>{user?.email}</Typography>
                    </Box>

                    <Box>
                        <Typography variant="caption" fontWeight="bold" color="text.secondary">
                            TELÉFONO
                        </Typography>
                        <Typography>{user?.phone}</Typography>
                    </Box>

                    <Box>
                        <Typography variant="caption" fontWeight="bold" color="text.secondary">
                            TIPO DE PERFIL
                        </Typography>
                        <Typography>{user?.type}</Typography>
                    </Box>
                </Stack>
            </Paper>

            <Stack direction="row" spacing={2} mt={3}>
                <Paper
                    sx={{
                        flex: 1,
                        p: 2,
                        textAlign: "center",
                        bgcolor: "#bfdbfe",
                    }}
                >
                    <Typography variant="h5" fontWeight="bold">
                        {user?.sharedBooksCount}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                        Libros Prestados
                    </Typography>
                </Paper>

                <Paper
                    sx={{
                        flex: 1,
                        p: 2,
                        textAlign: "center",
                        bgcolor: "#bfdbfe",
                    }}
                >
                    <Typography variant="h5" fontWeight="bold">
                        {user?.readBooksCount}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                        Libros Leídos
                    </Typography>
                </Paper>
            </Stack>
        </Box>
    );
};

const ProfileSection = ({ user, books, totalBooks, handleDeleteButton, query, onFilterChange, onSortChange, onPageChange }) => {
    return (
        <Box className="flex-1 p-4">
            <Box className="p-2">
                <Box display="flex" justifyContent="space-between" alignItems="center">
                    <Typography variant="h6" fontWeight="bold">
                        Gestión de Mis Libros
                    </Typography>
                    {user?.roles.includes("Publicador") &&
                        <NavLink to={"/bookEdit"}>
                            <Button variant="contained">Agregar nuevo libro</Button>
                        </NavLink>
                    }
                </Box>
            </Box>
            {user?.roles.includes("Publicador") ? (
                <Paper className="flex flex-col gap-4 p-4">
                    <ToggleButtonGroup
                        value={query.filterBy}
                        exclusive
                        onChange={onFilterChange}
                        size="small"
                    >
                        <ToggleButton value="ALL">Todos</ToggleButton>
                        <ToggleButton value="AVAILABLE">Disponibles</ToggleButton>
                        <ToggleButton value="BORROWED">Prestados</ToggleButton>
                        <ToggleButton value="DELETED">Borrados</ToggleButton>
                    </ToggleButtonGroup>

                    <Table className="divide-y divide-gray-200">
                        <TableHead>
                            <TableRow sx={{ bgcolor: "#facc40" }}>
                                <TableCell className="cursor-pointer hover:bg-yellow-200" sx={{ width: "50%" }} onClick={() => onSortChange("TITLE")} >
                                    Título y Autor {query.orderBy === "TITLE" && (query.isAsc ? "↑" : "↓")}
                                </TableCell>
                                <TableCell className="cursor-pointer hover:bg-yellow-200" sx={{ width: "12%" }} onClick={() => onSortChange("BY_AVAILABILITY")} align="center">
                                    Disponible {query.orderBy === "BY_AVAILABILITY" && (query.isAsc ? "↑" : "↓")}
                                </TableCell>
                                <TableCell className="cursor-pointer hover:bg-yellow-200" sx={{ width: "12%" }} onClick={() => onSortChange("ADDED_DATE")} align="center">
                                    Agregado {query.orderBy === "ADDED_DATE" && (query.isAsc ? "↑" : "↓") }
                                </TableCell>
                                <TableCell sx={{ width: "12%" }} align="center">Visitas recibidas</TableCell>
                                <TableCell sx={{ width: "14%" }} align="center">Acciones</TableCell>
                            </TableRow>
                        </TableHead>

                        <TableBody>
                            {books.map((book, i) => (
                                <BookRow key={i} book={book} onToggleActiveClick={() => handleDeleteButton(book.id, book.isActive)} />
                            ))}
                        </TableBody>
                    </Table>
                    <Stack alignItems="center" mt={3}>
                        <Pagination
                            count={Math.ceil((totalBooks || 0) / 3)}
                            page={query.page}
                            onChange={onPageChange}
                            color="primary"
                        />
                    </Stack>
                </Paper>
            ) : (
                <span>Necesitas un perfil "Publicador" para tener tus propios libros</span>
            )}
        </Box >
    );
};

export default Profile;