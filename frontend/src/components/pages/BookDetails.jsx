import BasicModal from "../ui/Modal/BasicModal"
import { useState, useEffect} from "react"
import { useParams, useSearchParams } from "react-router-dom"
import { useOnInit } from '../../hooks/useOnInIt'
import { getBookDetailById } from "../../api/services/BookService"
import { Button, Rating } from "@mui/material"
import { useAuth } from "../../context/AuthContext"
import { DatePicker } from "@mui/x-date-pickers"
import { LocalizationProvider } from "@mui/x-date-pickers"
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs"
import dayjs from "dayjs"
import useAlert from "../../hooks/useAlert"
import errorHandler from "../../hooks/errorHandler"
import { getRatingsFromBook } from "../../api/services/RatingService"
import { createReservation, getBibliokarma } from "../../api/services/ResservationService"

const BookCover = ({coverUrl}) => {
    return (
        <div className="justify-self-center bg-black w-fit h-fit rounded-lg shrink-0 p-8 ">
            <img
                src={coverUrl}
                alt="book-cover"
                className="max-w-xs h-72 object-cover bg-black"
            />
        </div>
    )
}

const BookMainInfo = ({genre, ratingAverage, title, author, description}) => {
    return (
        <section className="flex flex-col gap-4">
            <div className="flex items-center gap-2">
                <span className="text-sm inline-block bg-blue-100 rounded text-blue-400 font-bold px-2 py-1">{genre}</span>
                <Rating className="text-gray-500" value={ratingAverage.toFixed(1)} max={5} precision={0.1} readOnly/>
                <span>({ratingAverage.toFixed(1)})</span>
            </div>
            <h1 className="text-5xl font-bold">{title}</h1>
            <div className="flex items-center gap-4">
                <img
                    src="autor.png"
                    alt="autor-foto"
                    className="w-15 h-15 rounded-full object-cover bg-black"
                />
                <div className="flex flex-col">
                    <span className="text-sm text-gray-500">AUTOR</span>
                    <span className="text-xl font-semibold">{author}</span>
                </div>
            </div>
            <div>
                <h2 className="text-xl font-bold">Sinopsis</h2>
                <p>
                    {description}
                </p>
            </div>
        </section>
    )
}

const BookReservation = ({startDate, endDate, user, book, updateDatesAndFetchBibliokarma, onReservationSuccess}) => {
    const daysReserved = (new Date(endDate) - new Date(startDate)) / (1000 * 60 * 60 * 24) + 1;
    const { showSuccess } = useAlert()

    const shouldDisableDate = (date) => {
        return book.reservationsDates.some(range => {
            const start = dayjs(range.startDate)
            const end = dayjs(range.endDate)

            return date.isAfter(start.subtract(1, "day")) &&
                date.isBefore(end.add(1, "day"))
        })
    }

    const handleCreateReservation = async () => {
        try{
            await createReservation(book?.id, startDate, endDate)
            await onReservationSuccess()
            showSuccess("Reserva creada exitosamente!")
            
        }catch(error){
            errorHandler(error)
        }
    }

    return (
        <section className="flex flex-col bg-white rounded-xl border border-green-100 max-w-xs gap-4 p-4">
            <h2 className="text-xl font-bold">📆 Tu Reserva</h2>
            <div className="flex flex-col py-4">
                <LocalizationProvider dateAdapter={AdapterDayjs}>
                    <span className="text-sm text-gray-500">FECHAS SELECCIONADAS:</span>
                    <div className="flex flex-col gap-6 py-4">
                        <DatePicker
                            label="Recogido"
                            value={dayjs(startDate)}
                            onChange={(newValue) => {
                                const newStartDate = newValue.format("YYYY-MM-DD")
                                const adjustedEnd = endDate < newStartDate ? newStartDate : endDate
                                updateDatesAndFetchBibliokarma(newStartDate, adjustedEnd)
                            }}
                            shouldDisableDate={shouldDisableDate}
                            minDate={dayjs()}
                            slotProps={{ textField: { fullWidth: true } }}
                        />

                        <DatePicker
                            label="Devolución"
                            value={dayjs(endDate)}
                            onChange={(newValue) => {
                                const newEndDate = newValue.format("YYYY-MM-DD")
                                updateDatesAndFetchBibliokarma(startDate, newEndDate)
                            }}
                            shouldDisableDate={shouldDisableDate}
                            minDate={dayjs(startDate)}
                            slotProps={{ textField: { fullWidth: true } }}
                        />
                        <div className="flex justify-between">
                            <span className="text-sm text-gray-500">Duración</span>
                            <strong className="text-sm text-blue-500">{daysReserved} Días</strong>
                        </div>
                    </div>
                </LocalizationProvider>
            </div>
            <Button 
                variant="contained"
                onClick={() => handleCreateReservation()}
                disabled={!user?.roles.includes("Lector")}
            >
                Confirmar Reserva
            </Button>
            {user?.roles.includes("Lector") ? (
                <span className="text-xs text-center text-gray-400 mx-4">Con la reserva no se encontrará disponible en el rango de fechas</span>
            ):(
                <span className="text-xs text-center text-orange-400 mx-4">Necesitas un perfil Lector para realizar reservas.</span>
            )}
        </section>
    )
}

const BookMetadataItem = ({ label, value, containerClass = "", valueClass = "" }) => {
    return (
        <div className={`flex flex-col ${containerClass}`}>
            <span className="text-sm text-gray-500">{label}</span>
            <span className={`font-semibold ${valueClass}`}>{value}</span>
        </div>
    )
}

const BookMetadata = ({book, bibliokarma, ratingAverage}) => {
    return (
        <section className="flex flex-col bg-white rounded-xl gap-2 p-4">
            <h2 className="text-xl font-bold">Detalle del Libro</h2>
            <div className="grid grid-cols-4 gap-6">
                <BookMetadataItem
                    label="TIPO"
                    value={book.type}
                    containerClass="col-span-2"
                    valueClass="text-xs text-orange-500 bg-orange-100 rounded-lg w-fit py-1 px-2"
                >
                </BookMetadataItem>
                <BookMetadataItem
                    label="BIBLIOKARMAS"
                    value={bibliokarma}
                    containerClass="col-start-4"
                    valueClass="text-xs text-blue-500 bg-blue-100 rounded-lg w-fit py-1 px-2"
                >
                </BookMetadataItem>
                <BookMetadataItem label="GENERO" value={book.genre}></BookMetadataItem>
                <BookMetadataItem label="PÁGINAS" value={book.pages}></BookMetadataItem>
                <BookMetadataItem label="IDIOMA" value={book.language}></BookMetadataItem>
                <BookMetadataItem label="EDITORIAL" value={book.editorial}></BookMetadataItem>
                <BookMetadataItem label="ISBN-13" value={book.isbn}></BookMetadataItem>
                <BookMetadataItem label="PUBLICADO" value={book.publicationDate}></BookMetadataItem>
                <BookMetadataItem label="ESTADO" value={book.state}></BookMetadataItem>
                <BookMetadataItem label="PUNTAJE" value={ratingAverage.toFixed(1)}></BookMetadataItem>
            </div>
        </section>
    )
}

const ReviewCard = ({ name, image, rating, text }) => {
    return (
        <div className="flex flex-col gap-2">
            <div className="flex items-center gap-2">
                <img
                    src={image}
                    alt="autor-foto"
                    className="w-10 h-10 rounded-full object-cover bg-black"
                />
                <div className="flex flex-col">
                    <span className="text-sm text-gray-500">{name}</span>
                    <Rating className="text-gray-500" value={rating} max={5} precision={0.1} readOnly/>
                </div>
            </div>
            <p>{text}</p>
        </div>
    )
}

export {ReviewCard};

const BookReviews = ({ openModal , ratingCount, ratings}) => {
    return (
        <section className="flex flex-col gap-4">
            <div className="flex justify-between">
                <h2 className="text-xl font-bold">Reseñas de la Comunidad</h2>
                {ratingCount == 0 ? (
                    <span className="text-blue-500">No hay reseñas aún</span>
                ) : ratingCount > 2 &&(
                    <button onClick={openModal} className="text-blue-500 cursor-pointer">Ver {ratingCount} reseñas ➡️</button>
                )}
            </div>
            <div className="grid grid-cols-2 gap-4">
                {ratings.length > 0 && ratings.map((r, index) => (
                    <ReviewCard
                    key={index}
                    name={r.username}
                    image="autor.png"
                    rating={r.calification}
                    text={r.comment}
                    ></ReviewCard>
                ))}
            </div>
        </section>
    )
}

const ReviewsModal = ({ open, handleClose, idBook }) => {
    const [ratings, setRatings] = useState([])

    useEffect(() => {
        let isMounted = true

        const fetchData = async () => {
            if (!idBook || !open) return
            try {
                const response = await getRatingsFromBook(idBook)
                if (isMounted) {
                    setRatings(response.data ?? [])
                }
            } catch (error) {
                if (isMounted) setRatings([])
                errorHandler(error)
            }
        }
        fetchData()

        return () => {
            isMounted = false
        }
    }, [idBook, open])

    return (
        <section>
            <BasicModal open={open} handleClose={handleClose}>
                <h2 className="text-2xl font-bold mb-6">Todas las reseñas</h2>
                <div className="flex flex-col gap-4 max-h-[40vh] overflow-y-auto pr-2">
                    {ratings.length > 0 &&
                        ratings.map((r, index) => (
                            <ReviewCard
                                key={index}
                                name={r.username}
                                image="autor.png"
                                rating={r.calification}
                                text={r.comment}
                            />
                        ))}
                </div>
            </BasicModal>
        </section>
    )
}

const BookDetail = () => {
    const {user, refreshHeaderData} = useAuth()
    const {idBook} = useParams()

    const [searchParams] = useSearchParams();
    const from = searchParams.get("from");
    const to = searchParams.get("to");

    const [book, setBook] = useState({
        id: '',
        title: '',
        description: '',
        coverUrl: '',
        author: '',
        pages: '',
        isbn: '',
        type: '',
        language: '',
        editorial: '',
        publicationDate: '',
        state: '',
        genre: '',
        ratingAverage: '',
        ratingCount: '',
        firstTwoRatings: [],
        reservationsDates: [],
        bibliokarma: ''
    })
    const [bibliokarma, setBibliokarma] = useState(null)
    
    const [reservationStartDate, setReservationStartDate] = useState(from)
    const [reservationEndDate, setReservationEndDate] = useState(to)

    const [openModalRating, setOpenModalRating] = useState(false)

    const openModal = () =>  setOpenModalRating(true)
    const closeModal = () => setOpenModalRating(false)

    const fetchData = async () => {
        try {
            const response = await getBookDetailById(idBook, from, to)
            setBook(response.data)
            setBibliokarma(response.data.bibliokarma)
        }
        catch (error) {
            errorHandler(error)
        }
    }
   
    const updateDatesAndFetchBibliokarma = async (newStartDate, newEndDate) => {
        setReservationStartDate(newStartDate)
        setReservationEndDate(newEndDate)
        try{
            const response = await getBibliokarma(
                idBook,
                newStartDate,
                newEndDate
            )
            setBibliokarma(response.data.bibliokarma)
        }
        catch(error){
            errorHandler(error)
        }
    }

    const handleReservationSuccess = async () => {
        await updateDatesAndFetchBibliokarma(reservationStartDate, reservationEndDate)
        refreshHeaderData()
    }

    useOnInit(() => {
        fetchData()
    })
    
    if (!book.id) return <div>Loading...</div>

    return (
        <div className="flex flex-col max-w-7xl mx-auto bg-gray-100 px-8 py-8">
            <p className="text-sm text-gray-500">
                Explorar {'>'} <strong className="text-black">Nombre del libro</strong>
            </p>
            <div className="grid grid-cols-[auto_1fr] auto-rows-min gap-8 mt-6">
                <BookCover coverUrl={book.coverUrl}></BookCover>
                <BookMainInfo genre={book.genre} ratingAverage={book.ratingAverage} title={book.title} author={book.author} description={book.description}></BookMainInfo>
                <div className="row-span-2">
                    <BookReservation 
                        startDate={reservationStartDate}  
                        endDate={reservationEndDate} 
                        user = {user}
                        book={book}
                        updateDatesAndFetchBibliokarma={updateDatesAndFetchBibliokarma}
                        onReservationSuccess={handleReservationSuccess}
                    >
                    </BookReservation>
                </div>
                <BookMetadata book={book} bibliokarma={bibliokarma} ratingAverage={book.ratingAverage}></BookMetadata>
                <BookReviews openModal={openModal} ratingCount={book.ratingCount} ratings={book.firstTwoRatings}></BookReviews>
            </div>

            <ReviewsModal open={openModalRating} handleClose={closeModal} idBook={idBook}/>
        </div>
    )
}

export default BookDetail;
