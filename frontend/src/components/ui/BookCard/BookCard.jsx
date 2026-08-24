import { Card, CardMedia, CardContent, Typography, Box, Chip } from "@mui/material";
import { NavLink } from "react-router-dom";

const BookCard = ({ book, from, to }) => {

    return (
        <Card sx={{ width: 256, m: 2, borderRadius: 4, overflow: "hidden", borderRight: "4px solid", borderBottom: "4px solid", "&:hover": { border: "0px", borderLeft: "4px solid", borderTop: "4px solid" }, transition: "border 0.1s" }}>

            <NavLink to={`/bookDetails/${book.id}?from=${from}&to=${to}`} sx={{cursor: "pointer"}}>
                <CardMedia
                component="img"
                image={book.coverUrl}
                alt="book"
                className="h-64 object-cover"
            />
            </NavLink>

            <CardContent className="h-80">
                <Box display="flex" justifyContent="space-between" mb={1}>
                    <Chip label={book.genre} color="primary" size="small" />
                    <Typography variant="body2">★ {book.rating.toFixed(1)}</Typography>
                </Box>

                <Typography variant="h6" fontWeight="bold">
                    {book.title}
                </Typography>

                <Typography variant="body2" color="text.secondary" mb={2}>
                    {book.author}
                </Typography>

                <Box className="grid grid-cols-2 gap-4 text-sm p-2">
                    <Box>
                        <Typography fontWeight="medium">ISBN</Typography>
                        <Typography variant="body2">{book.isbn}</Typography>
                    </Box>

                    <Box>
                        <Typography fontWeight="medium">Idioma</Typography>
                        <Typography variant="body2">{book.language}</Typography>
                    </Box>

                    <Box>
                        <Typography fontWeight="medium">Tipo</Typography>
                        <Typography variant="body2">{book.booktype}</Typography>
                    </Box>

                    <Box>
                        <Typography fontWeight="medium">Estado</Typography>
                        <Chip label={book.status} size="small" color="info" />
                    </Box>
                </Box>
            </CardContent>

            <Box sx={{ p: 2, bgcolor: "grey.200" }}>
                <Typography variant="body2" fontWeight="medium">
                    {book.publisher}
                </Typography>
            </Box>
        </Card>
    );
};

export default BookCard;