import { useEffect, useRef, useState } from 'react'
import BookCard from '../ui/BookCard/BookCard'
import { Box, Button, Checkbox, FormControl, FormControlLabel, FormGroup, InputLabel, MenuItem, Pagination, Select, Slider, Stack, TextField, Typography } from '@mui/material'
import { filterBooks } from '../../api/services/BookService'
import { useForm, Controller } from "react-hook-form"
import { yupResolver } from "@hookform/resolvers/yup"
import * as yup from "yup"


const HomeAsideForm = ({ control, handleSubmit, onSubmit, reset }) => {

    const genresOptions = [
        "Drama",
        "Ciencia ficción",
        "Autoayuda",
        "Romance",
        "Diseño",
        "Literatura clásica"
    ]

    return (
        <Box className="max-w-80 min-w-42 sticky top-2 h-fit border border-gray-300 rounded-xl p-5 bg-white">
            <form className="flex flex-col gap-4" onSubmit={handleSubmit(onSubmit)}>

                <div>
                    <legend className="font-semibold">Género</legend>
                    <FormGroup>
                        {genresOptions.map((genre) => (
                            <FormControlLabel key={genre} className="transition-colors duration-200 hover:text-blue-600"
                                control={
                                    <Controller
                                        name="genres"
                                        control={control}
                                        render={({ field }) => (
                                            <Checkbox
                                                checked={field.value.includes(genre)}
                                                onChange={(e) => {
                                                    const newValue = e.target.checked
                                                        ? [...field.value, genre]
                                                        : field.value.filter(g => g !== genre)
                                                    field.onChange(newValue)
                                                }}
                                            />
                                        )}
                                    />
                                }
                                label={genre}
                            />
                        ))}
                    </FormGroup>
                </div>

                <div>
                    <legend className="font-semibold">Rango de páginas</legend>
                    <Controller
                        name="minPages"
                        control={control}
                        render={({ field: minField }) => (
                            <Controller name="maxPages"
                                control={control}
                                render={({ field: maxField, fieldState: { error } }) => (
                                    <>
                                        <Slider
                                            value={[minField.value, maxField.value]}
                                            onChange={(_, value) => {
                                                minField.onChange(value[0])
                                                maxField.onChange(value[1])
                                            }}
                                            valueLabelDisplay="auto"
                                            min={0}
                                            max={1500}
                                        />
                                        {error && (
                                            <p className="text-red-500 text-xs">{error.message}</p>
                                        )}
                                    </>
                                )}
                            />
                        )}
                    />
                </div>

                <div>
                    <legend className="font-semibold">Rango de fechas</legend>
                    <div className="flex gap-2 pt-2">
                        <Controller name="from"
                            control={control}
                            render={({ field }) => (
                                <TextField
                                    label="Desde"
                                    type="date"
                                    value={field.value || ""}
                                    onChange={field.onChange}
                                    slotProps={{ inputLabel: { shrink: true } }}
                                    fullWidth
                                />
                            )}
                        />
                        <Controller name="to"
                            control={control}
                            render={({ field, fieldState: { error } }) => (
                                <TextField
                                    label="Hasta"
                                    type="date"
                                    value={field.value || ""}
                                    onChange={field.onChange}
                                    slotProps={{ inputLabel: { shrink: true } }}
                                    error={!!error}
                                    helperText={error?.message}
                                    fullWidth
                                />
                            )}
                        />
                    </div>
                </div>

                <div>
                    <p className="font-semibold">Detalles</p>
                    <div className="flex flex-col gap-2 mt-2">
                        <Controller name="isbn"
                            control={control}
                            render={({ field, fieldState: { error } }) => (
                                <TextField
                                    {...field}
                                    label="ISBN"
                                    error={!!error}
                                    helperText={error?.message}
                                    fullWidth
                                />
                            )}
                        />
                        <Controller name="username"
                            control={control}
                            render={({ field, fieldState: { error } }) => (
                                <TextField
                                    {...field}
                                    label="Prestado por"
                                    error={!!error}
                                    helperText={error?.message}
                                    fullWidth
                                />
                            )}
                        />
                    </div>
                </div>

                <Button type="submit" variant="contained">
                    Aplicar filtros
                </Button>

                <Button variant="outlined"
                    onClick={() => {
                        reset()
                        handleSubmit(onSubmit)()
                    }}>
                    Resetear filtros
                </Button>
            </form>
        </Box>
    )
}

const HomeHeader = ({ control, handleSubmit, onSubmit }) => {
    return (
        <Box className="p-6 border border-gray-300 rounded-xl bg-white">
            <Typography variant="h5" fontWeight="bold">
                Encuentra tu próxima lectura
            </Typography>

            <Typography variant="body2" color="text.secondary">
                Explora miles de libros disponibles para préstamo en nuestra comunidad
            </Typography>

            <Box
                component="form"
                onSubmit={handleSubmit(onSubmit)}
                sx={{
                    display: "flex",
                    alignItems: "center",
                    gap: 1,
                    mt: 2
                }}
            >
                <Controller
                    name="title"
                    control={control}
                    render={({ field }) => (
                        <TextField
                            {...field}
                            placeholder="Buscar por título..."
                            size="small"
                            fullWidth
                        />
                    )}
                />

                <Button type="submit" variant="contained">
                    Buscar
                </Button>
            </Box>
        </Box>
    )
}

const HomePopularSection = ({ control, books, totalBooks, booksPerPage, currentPage, onPageChange, from, to }) => {

    return (
        <Box className="p-6 border-b border-gray-300 rounded-xl bg-white">
            <Stack
                direction="row"
                justifyContent="space-between"
                alignItems="center"
                spacing={2}
            >
                <Typography variant="h5" fontWeight="bold">
                    Libros Populares
                </Typography>

                <Stack direction="row" spacing={1} alignItems="center">
                    <Typography variant="body2">Ordenar por:</Typography>

                    <FormControl size="small">
                        <InputLabel id="order-label">Orden</InputLabel>
                        <Controller
                            name="order"
                            control={control}
                            render={({ field }) => (
                                <Select {...field} label="Orden">
                                    <MenuItem value="TITLE">Titulo</MenuItem>
                                    <MenuItem value="AUTHOR">Autor</MenuItem>
                                    <MenuItem value="PUBLISHER">Publicador</MenuItem>
                                    <MenuItem value="ADDED_DATE">Fecha publicación</MenuItem>
                                </Select>
                            )}
                        />
                    </FormControl>
                </Stack>
            </Stack>

            <Box
                sx={{
                    display: "flex",
                    flexWrap: "wrap",
                    justifyContent: "center",
                    mt: 3,
                    gap: 2
                }}
            >
                {books.map((book, i) => (
                    <BookCard
                        key={i}
                        book={book}
                        from={from}
                        to={to}
                    />
                ))}
            </Box>

            <Stack alignItems="center" mt={3}>
                <Pagination 
                    count={Math.ceil((totalBooks || 0) / booksPerPage)} 
                    page={currentPage} 
                    onChange={onPageChange} 
                    color="primary" 
                />
            </Stack>
        </Box>
    )
}

const Home = () => {
    const booksPerPage = 6
    const [books, setBooks] = useState([])
    const [totalBooks, setTotalBooks] = useState(0)
    const [currentPage, setCurrentPage] = useState(1)
    const isFirstLoad = useRef(true)

    const today = new Date().toISOString().split("T")[0]
    const defaultFilters = {
        genres: [],
        minPages: 0,
        maxPages: 1500,
        from: today,
        to: today,
        isbn: "",
        username: "",
        order: "TITLE",
        title: ""
    }

    const normalizeFilters = (data = {}) => ({
        ...data,
        genres: !data.genres || data.genres.length === 0 ? null : data.genres,
        minPages: data.minPages === 0 ? null : data.minPages,
        maxPages: data.maxPages === 1500 ? null : data.maxPages,
        from: data.from || null,
        to: data.to || null,
        isbn: !data.isbn ? null : data.isbn,
        username: !data.username ? null : data.username,
        title: !data.title ? null : data.title,
    })

    const [appliedFilters, setAppliedFilters] = useState(normalizeFilters(defaultFilters))

    const fetchBooks = async (page = 1, filtersData = {}) => {
        const dataFormat = normalizeFilters(filtersData)

        const filters = {
            ...dataFormat,
            isFilter: !isFirstLoad.current
        }

        const response = await filterBooks(filters, page, booksPerPage)

        const data = response.data
        setBooks(data.books)
        setTotalBooks(data.totalFilteredBooks)

        isFirstLoad.current = false
    }

    const handlePageChange = (_, value) => {
        setCurrentPage(value)
    }

    const schema = yup.object({
        genres: yup.array().of(yup.string()),
        minPages: yup.number().nullable(),
        maxPages: yup.number().nullable(),
        from: yup.string().nullable().transform((val) => val === "" ? null : val),
        to: yup.string().nullable().transform((val) => val === "" ? null : val)
            .test(
                "is-after",
                "'Hasta' debe ser posterior a 'desde'",
                function (value) {
                    const { from } = this.parent
                    if (!from || !value) return true
                    return value >= from
                }
            ),
        isbn: yup.string().nullable().transform((val) => val === "" ? null : val),
        username: yup.string().nullable().transform((val) => val === "" ? null : val),
        order: yup.string().nullable(),
        title: yup.string().nullable().transform((val) => val === "" ? null : val),
    })

    const { control, handleSubmit, watch, reset } = useForm({
        defaultValues: defaultFilters,
        resolver: yupResolver(schema)
    })

    const order = watch("order")
    const from = watch("from")
    const to = watch("to")

    const onSubmit = async (data) => {
        const formattedData = normalizeFilters(data)
        setCurrentPage(1)
        setAppliedFilters(formattedData)
    }

    useEffect(() => {
        fetchBooks(currentPage, appliedFilters)
    }, [appliedFilters, currentPage])

    return (
        <Box className="w-full mx-auto flex gap-6 p-4">
            <HomeAsideForm
                control={control}
                handleSubmit={handleSubmit}
                onSubmit={onSubmit}
                reset={reset}
            />

            <main className="flex flex-col flex-1 gap-6">
                <HomeHeader
                    control={control}
                    handleSubmit={handleSubmit}
                    onSubmit={onSubmit}
                />

                <HomePopularSection
                    control={control}
                    books={books}
                    booksPerPage={booksPerPage}
                    totalBooks={totalBooks}
                    currentPage={currentPage}
                    onPageChange={handlePageChange}
                    from={from}
                    to={to}
                />
            </main>
        </Box>
    )
}

export default Home