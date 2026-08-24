import { useState } from 'react';
import { useParams } from "react-router-dom"

import { getBookEditableFields } from "../../api/services/BookService";

import { Typography, Box, Stack, TextField, MenuItem, Button, InputAdornment, IconButton, Alert } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';

import { useOnInit } from '../../hooks/useOnInIt';
import errorHandler from '../../hooks/errorHandler';
import { useAuth } from '../../context/AuthContext';

const BookForm = ({ book, setBook, handleSaveBook, content }) => {
    const [coverUrlError, setCoverUrlError] = useState('');
    const [isbnError, setIsbnError] = useState('');
    const [submitMessage, setSubmitMessage] = useState('');
    const [submitSeverity, setSubmitSeverity] = useState('success');
        
    const isValidUrl = (value) => {
        if (!value?.trim()) return false;

        try {
            const parsedUrl = new URL(value);
            const isHttp = parsedUrl.protocol === 'http:' || parsedUrl.protocol === 'https:';
            const hasImageExtension = /\.(jpg|jpeg|png|gif|webp|avif|bmp|svg)(\?.*)?(#.*)?$/i.test(parsedUrl.pathname + parsedUrl.search + parsedUrl.hash);

            return isHttp && hasImageExtension;
        } catch {
            return false;
        }
    };

    const hasImage = isValidUrl(book.coverUrl);

    const isValidIsbn = (value) => /^(97[89])\d{10}$/.test((value ?? '').trim());

    const handleChange = (e) => {
        setBook({ ...book, [e.target.name]: e.target.value });

        if (e.target.name === 'coverUrl') {
            setCoverUrlError('');
        }

        if (e.target.name === 'isbn') {
            setIsbnError('');
        }
    }
    
    const clearCoverUrl = () => {
        setBook({ ...book, coverUrl: "" });
    }
    
    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!isValidUrl(book.coverUrl)) {
            setCoverUrlError('Ingresa una URL válida que termine en una extensión de imagen (jpg, png, webp, etc).');
            return;
        }

        if (!isValidIsbn(book.isbn)) {
            setIsbnError('ISBN: debe empezar con 979 o 978, y tener 13 dígitos.');
            return;
        }

        const bookObj = {
            ...book,
            pages: Number(book.pages)
        }

        const success = await handleSaveBook(bookObj)

        if (success) {
            setSubmitSeverity('success')
            setSubmitMessage(content.successMessage)
            return
        }

        setSubmitSeverity('error')
        setSubmitMessage(content.errorMessage)
    };
        
    return (
        <form onSubmit={handleSubmit} className='flex gap-8'>
            
            <div className='bg-white w-1/3 h-max flex items-center justify-center p-6 rounded-lg'>
                <Stack className='h-auto items-center gap-4'>

                    <Box
                        className='relative w-full aspect-[740/774] max-h-[80vh] flex items-center justify-center p-2'>
                        {hasImage ? (
                            <img
                                src={book.coverUrl}
                                alt="Portada del libro"
                                className="w-full h-full object-contain rounded-md"
                            />
                        ) : (
                            <Typography variant="h6" color="text.secondary">
                                Sin imágen
                            </Typography>
                        )}
                    </Box>

                    <TextField
                        fullWidth
                        required
                        label="Link de imagen"
                        name="coverUrl"
                        value={book.coverUrl}
                        onChange={handleChange}
                        error={Boolean(coverUrlError)}
                        helperText={coverUrlError}
                        InputProps={{
                            endAdornment: (
                                <InputAdornment position="end">
                                    <IconButton
                                        onClick={clearCoverUrl}
                                        edge="end"
                                        aria-label="Limpiar link de imagen"
                                        size="small"
                                    >
                                        <CloseIcon fontSize="small" />
                                    </IconButton>
                                </InputAdornment>
                            )
                        }}
                    />

                </Stack>
            </div>

            <div className='bg-white rounded-lg w-2/3 h-auto flex flex-col justify-center p-6 shadow-sm'>

                <div className="grid grid-cols-2 grid-rows-5 grid-flow-col gap-3">

                    <TextField fullWidth required label="Título" name="title" onChange={handleChange} value={book.title} />

                    <TextField fullWidth required label="Autor" name="author" onChange={handleChange} value={book.author} />

                    <TextField fullWidth required label="Editorial" name="editorial" onChange={handleChange} value={book.editorial} /> 

                    <TextField fullWidth required label="ISBN" name="isbn" onChange={handleChange} error={Boolean(isbnError)} helperText={isbnError} value={book.isbn} />

                    <TextField fullWidth required type="number" label="Cantidad de páginas" name="pages" inputProps={{ min: 1 }} onChange={handleChange} value={book.pages} />

                    <TextField select fullWidth required label="Tipo" name='bookType' value={book.bookType} onChange={handleChange} disabled={!content.allowTypeEdition}>
                        {["Común", "Con dedicatoria", "Coleccionable"].map((option) => <MenuItem key={option} value={option}>{option}</MenuItem>)}
                    </TextField>

                    <TextField select fullWidth required label="Género" name='genre' value={book.genre} onChange={handleChange}>
                        {["Drama", "Ciencia ficción", "Romance", "Autoayuda", "Diseño", "Literatura clásica"].map((option) => <MenuItem key={option} value={option}>{option}</MenuItem>)}
                    </TextField>

                    <TextField select fullWidth required label="Idioma" name='language' value={book.language} onChange={handleChange}>
                        {["Español", "Inglés", "Francés", "Portugués"].map((option) => <MenuItem key={option} value={option}>{option}</MenuItem>)}
                    </TextField>

                    <TextField select fullWidth required label="Estado" name='state' value={book.state} onChange={handleChange}>
                        {["Excelente", "Muy bueno", "Bueno", "Regular", "Malo"].map((option) => <MenuItem key={option} value={option}>{option}</MenuItem>)}
                    </TextField>

                    <TextField fullWidth required type="date" label="Fecha de publicación" name="publicationDate" InputLabelProps={{ shrink: true }} onChange={handleChange} value={book.publicationDate} />
                </div>

                <TextField
                    fullWidth required multiline rows={4} label="Descripción" name="description"
                    value={book.description}
                    onChange={handleChange}
                    inputProps={{ maxLength: 500 }}
                    helperText={`${book.description.length}/500`}
                    sx={{ mt: 2 }}
                />

                <Button type="submit" variant="contained" fullWidth size="large" sx={{ mt: 1, width: 220, display: "block", mx: "auto" }}>
                    {content.submitButtonLabel}
                </Button>

                {submitMessage && (
                    <Alert severity={submitSeverity} sx={{ mt: 2 }}>
                        {submitMessage}
                    </Alert>
                )}
            </div>
        </form>
    );
}

const BookEdit = ({ formContent }) => {

    const { idBook } = useParams();
    const { user } = useAuth();
    const [book, setBook] = useState({
        title: '',
        author: '',
        editorial: '',
        isbn: '',
        pages: '',
        bookType: '',
        genre: '',
        language: '',
        state: '',
        publicationDate: '',
        description: '',
        coverUrl: ''
    });

    const fetchBookById = async () => {
        if (!idBook) return
        const response = await getBookEditableFields(idBook)
        setBook(response.data)
    }

    const handleSaveBook = async (bookObj) => {
        try {
            await formContent.saveBook({
                idBook,
                userId: user.id,
                bookObj
            })
            return true
        } catch (error) {
            errorHandler(error)
            return false
        }
    }

    useOnInit(() => {
        if (formContent.shouldLoadBook) { fetchBookById() }
    })

    return (
        <div className='w-2/3 min-h-screen'>

            <div className='flex flex-col justify-center p-6 mb-8'>
                <Typography variant="h5" fontWeight="bold" sx={{ mb: 2 }}>
                    {formContent.pageTitle}
                </Typography>
                <Typography variant="body1" color="text.secondary">
                    {formContent.pageDescription}
                </Typography>
            </div>

            <BookForm book={book} setBook={setBook} content={formContent} handleSaveBook={handleSaveBook} />
        </div>
    )
}

export default BookEdit;