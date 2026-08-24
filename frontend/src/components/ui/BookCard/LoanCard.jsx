import { Card, CardContent, Typography, Box, Chip, Button, CardMedia } from "@mui/material";
import { NavLink } from "react-router-dom";

const LoanCard = ({ prestamo, openModal, activeTab}) => {

    const getStatusConfig = (state) => {
        switch (state) {
            case "Próximo a vencer": return { color: "warning", label: "PRÓXIMO A VENCER" };
            case "Devuelto": return { color: "error", label: "DEVUELTO" };
            default: return { color: "success", label: "ACTIVO" };
        }
    };

    const { color, label } = getStatusConfig(prestamo.state);

    return (
        <Card sx={{ width: 256, height: 420, m: 2, borderRadius: 2, position: 'relative', boxShadow: 3}}>
            <Box position="absolute" top={0} right={0} zIndex={10}>
                <Chip 
                    label={label} 
                    color={color} 
                    size="small" 
                    sx={{ borderRadius: '0 8px 0 8px', fontWeight: 'bold' }} 
                />
            </Box>

            <NavLink to={`/bookDetails/${prestamo.idBook}?from=${prestamo.startDate}&to=${prestamo.endDate}`} sx={{cursor: "pointer"}}>
                <CardMedia
                    component="img"
                    height="192"
                    image={prestamo.coverBook}
                    alt={prestamo.title}
                    sx={{ objectFit: 'cover', height: 192 }}
                />
            </NavLink>


            <CardContent className="flex flex-col gap-1">
                <Typography variant="h6" fontWeight="bold" className="truncate">
                    {prestamo.title}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                    {prestamo.author}
                </Typography>

                <Box className="bg-gray-50 p-2 rounded text-sm mt-1">
                    <Typography sx={{ whiteSpace: "nowrap" }} variant="caption" display="inline-block" color="text.secondary">
                        <span className="font-bold text-gray-700">👤 Prestado por: </span> {prestamo.publisher}
                    </Typography>
                    <Typography sx={{ whiteSpace: "nowrap" }} variant="caption" display="inline-block" color="text.secondary">
                        <span className="font-bold text-gray-700">📅 Rango: </span>{prestamo.startDate} - {prestamo.endDate}
                    </Typography>
                    <Typography sx={{ whiteSpace: "nowrap" }} variant="caption" display="inline-block" color="primary" fontWeight="bold">
                        ⭐ Karma: {prestamo.bibliokarma}
                    </Typography>
                </Box>

                {activeTab === 0 && prestamo.state === "Devuelto" && (
                    <Button variant="contained" size="small" fullWidth sx={{ mt: 1 }} onClick={(e) => {
                        e.stopPropagation();
                        openModal();
                    }}>
                        Calificar
                    </Button>
                )}
            </CardContent>
        </Card>
    );
};

export default LoanCard;