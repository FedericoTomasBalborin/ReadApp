import { useState, useEffect, useCallback } from 'react';
import LoanCard from '../ui/BookCard/LoanCard';
import { Typography, Rating, Button, TextField, Stack, Pagination} from '@mui/material';
import { NavLink } from 'react-router-dom';
import BasicModal from '../ui/Modal/BasicModal.jsx';
import { useAuth } from "../../context/AuthContext"
import errorHandler from '../../hooks/errorHandler'
import { mapToMyReservations } from '../../api/domain/Reservation.js';
import { createRating } from '../../api/services/RatingService.js';
import useAlert from '../../hooks/useAlert.jsx';
import { getMyReservations, getOwnedBooksReservations } from '../../api/services/ResservationService.js';

const LoansHeader = () => (
    <div className="mb-6">
        <h1 className="text-4xl font-bold text-gray-900">Préstamos de libros</h1>
        <p className="text-gray-500 mt-2">
            Aquí vas a encontrar toda la información sobre el intercambio de libros con otros usuarios.
        </p>
    </div>
);

const LoansControls = ({activeTab, setActiveTab, busqueda, setBusqueda, onSubmit, isLector, isPublicador}) => {

    return (
        <div className="flex flex-col gap-4 border-b border-gray-200 pb-4">
            <div className="flex gap-8">
                {isLector && (
                    <button 
                        onClick={() => setActiveTab(0)}
                        className={`pb-2 font-semibold transition-colors ${activeTab === 0 ? 'text-blue-600 border-b-2 border-blue-600' : 'text-gray-400 hover:text-gray-600'}`}
                    >
                        Prestados a mí
                    </button>
                    )
                }
                {isPublicador && (
                    <button 
                        onClick={() => setActiveTab(1)}
                        className={`pb-2 font-semibold transition-colors ${activeTab === 1 ? 'text-blue-600 border-b-2 border-blue-600' : 'text-gray-400 hover:text-gray-600'}`}
                    >
                        Prestados por mí
                    </button>
                    )
                }
            </div>
            <form className='flex items-center' onSubmit={onSubmit}>
                    <input 
                    type="text"
                    placeholder="Buscar por título, autor..."
                    className="w-full p-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-gray-50"
                    value={busqueda}
                    onChange={(e) => setBusqueda(e.target.value)}
                />
                <Button type="submit" variant="contained" sx={{ width: 130, display: "block", mx: 2}}>
                    Buscar
                </Button>
            </form>
        </div>
    )
}

const Explorar = () => (
    <div className="mt-12 p-8 bg-blue-50 rounded-2xl flex flex-col items-center gap-4 text-center">
        <div className="text-3xl text-blue-600">📖</div>
        <h3 className="text-xl font-bold">¿Quieres leer algo nuevo?</h3>
        <p className="text-gray-600 max-w-md">
            Explora la biblioteca y solicita libros en préstamo de otros lectores de la comunidad.
        </p>
        <NavLink to={"/"}>
            <button className="bg-blue-600 text-white font-bold py-2 px-6 rounded-lg hover:bg-blue-700 transition-colors shadow-md cursor-pointer">
                Explorar Catálogo
            </button>
        </NavLink>
    </div>
);

const MyLoans = () => {
    const { showSuccess } = useAlert()
    const {user} = useAuth()
    const isLector = user?.roles?.includes("Lector")
    const isPublicador = user?.roles?.includes("Publicador")
    const [activeTab, setActiveTab] = useState(isLector ? 0 : 1); 
    const [busqueda, setBusqueda] = useState('');
    const [filter, setFilter] = useState('');
    const [reservations, setReservations] = useState([])
    const [currentPage, setCurrentPage] = useState(1)
    const [totalReservations, setTotalReservations] = useState(0)
    const reservationsPerPage = 4 

    const [openCalification, setOpenCalification] = useState(false);
    const [selectedLoan, setSelectedLoan] = useState(null);

    const [formData, setFormData] = useState({
        calification: 0,
        comment: "",
    });

    const openModal = (prestamo) => {
        setSelectedLoan(prestamo);
        setOpenCalification(true);
    };

    const closeModal = () => {
        setOpenCalification(false);
        setSelectedLoan(null);
        setFormData({ calification: 0, comment: "" }); 
    };

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try{
            await createRating(
                selectedLoan.idBook,
                formData.calification,
                formData.comment
            )
            showSuccess("Libro calilificado exitosamente!!!")
        } catch (error) {
            errorHandler(error);
        }

        closeModal();
    };

    const fetchReservations = useCallback(async () => { 
        try {
            setReservations([])
            const response = activeTab === 0 ? await getMyReservations(currentPage, reservationsPerPage, filter) : await getOwnedBooksReservations(currentPage, reservationsPerPage, filter);
            setReservations(response.data.reservations.map(dto => mapToMyReservations(dto)))
            setTotalReservations(response.data.totalReservations)
        }
        catch (error) {
            errorHandler(error)
        }
    }, [activeTab, currentPage, filter, user.id])

    const handlePageChange = (_, value) => {
        setCurrentPage(value)
    }

    const onSubmit = (e) => {
        e.preventDefault()
        setFilter(busqueda)
    }

    useEffect(() => {
        const load = async () => { await fetchReservations() }
        load()
    }, [fetchReservations])


    return (
        <div className="min-h-screen bg-white">
            <div className="w-7xl px-8 py-12 flex flex-col gap-6">
                
                <LoansHeader/>

                <LoansControls 
                    activeTab={activeTab} 
                    setActiveTab={setActiveTab} 
                    busqueda={busqueda} 
                    setBusqueda={setBusqueda} 
                    onSubmit={onSubmit}
                    isLector={isLector}
                    isPublicador={isPublicador}
                />

                {/* Grilla de Préstamos */}
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mt-6 min-h-[450px] items-start">
                    {reservations.length > 0 ? (
                        reservations.map((prestamo, i) => (
                            <LoanCard key={i} prestamo={prestamo} activeTab={activeTab} openModal={() => openModal(prestamo)}/>
                        ))
                        ) : (
                        <div className="col-span-full flex justify-center py-20">
                            <p className="text-gray-400">No se encontraron préstamos.</p>
                        </div>
                    )}
                </div>

                <Stack alignItems="center" mt={3}>
                    <Pagination 
                        count={Math.ceil((totalReservations || 0) / reservationsPerPage)} 
                        page={currentPage}
                        onChange={handlePageChange}
                        color="primary" 
                    />
                </Stack>

                <Explorar />
                
            </div>

        <BasicModal open={openCalification} handleClose={closeModal}>

            <h2 className="text-2xl font-bold mb-6">Califiación</h2>
            
            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                
                {/* Selector de Estrellas */}
                <div className="flex flex-col items-center gap-2 mt-4">
                    <Typography variant="caption" color="text.secondary" fontWeight="bold">
                        ¿Qué puntaje le das?
                    </Typography>
                    <Rating
                        name="calification"
                        size="large"
                        value={formData.calification}
                        onChange={(event, newValue) => {
                            setFormData({ ...formData, calification: newValue });
                        }}
                    />
                </div>

                <TextField
                    fullWidth 
                    required 
                    multiline 
                    rows={4} 
                    label="Tu reseña" 
                    name="comment" 
                    value={formData.comment} 
                    onChange={handleChange}
                    inputProps={{ maxLength: 500 }}
                    helperText={`${formData.comment.length}/500`}
                    sx={{ mt: 1 }}
                />

                <Button 
                    type="submit" 
                    variant="contained" 
                    fullWidth 
                    disabled={!formData.calification || !formData.comment}
                    sx={{ mt: 2 }}
                >
                    Enviar Calificación
                </Button>
            </form>
        </BasicModal>

        </div>
    );
};

export default MyLoans;