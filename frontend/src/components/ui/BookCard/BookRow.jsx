import { Box, Button, Stack, TableCell, TableRow, Typography } from "@mui/material"
import { NavLink } from "react-router-dom";

const BookRow = ( { book, onToggleActiveClick } ) => {
    return (
        <TableRow>
            <TableCell>
                <Box className="flex flex-row items-center gap-4">
                    <img src={book.coverUrl} alt="Book Cover" className="max-w-12" />
                    <Box>
                        <Typography variant="h6" fontWeight="bold">
                            {book.title}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                            {book.author}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                            {book.genre}
                        </Typography>
                    </Box>
                </Box>
            </TableCell>

            <TableCell align="center">
                {book.available ? "Disponible" : "Prestado"}
            </TableCell>

            <TableCell align="center">{book.addedDate}</TableCell>

            <TableCell align="center">{book.clickCount}</TableCell>

            <TableCell align="center">
                <Stack direction="row" spacing={1} justifyContent="center">
                    <NavLink to={`/bookEdit/${book.id}`}>
                        <Button variant="contained" color="success">
                            ✏️
                        </Button>
                    </NavLink>

                    <Button
                        variant="contained"
                        color={book.isActive ? "error" : "warning"}
                        onClick={() => onToggleActiveClick()}
                    >
                        {book.isActive ? "🗑️" : "♻️"}
                    </Button>
                </Stack>
            </TableCell>
        </TableRow>
    )
}

export default BookRow;