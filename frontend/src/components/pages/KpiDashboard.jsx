import getMetrics from "../../api/services/metricService";
import {
    Stack, Box, Typography, FormGroup, FormControlLabel, Checkbox
} from "@mui/material";
import { useState, useRef, useEffect } from "react";
import DynamicTable from "../ui/DynamicTable";

const KpiDashboard = () => {
    const [open, setOpen] = useState(false)
    const menuRef = useRef(null)
    const [recentActivity, setRecentActivity] = useState([])
    const [top5UsersBibliokarma, setTop5UsersBibliokarma] = useState([])
    const [conversionRate, setConversionRate] = useState([])
    const [ratingAnalysis, setRatingAnalysis] = useState([])
    const [catalogHealthStatus, setCatalogHealthStatus] = useState(null)
    const queryOptions = [
        { id: "activity", label: "Actividad reciente" },
        { id: "karma", label: "Top bibliokarma" },
        { id: "conversion", label: "Tasa de conversión" },
        { id: "ratings", label: "Análisis de calificaciones" },
        { id: "catalog", label: "Estado del catálogo" }
    ]

    const [selectedQueries, setSelectedQueries] = useState([])

    const handleCheckboxChange = (query) => {
        setSelectedQueries(prev =>
            prev.includes(query)
                ? prev.filter(q => q !== query)
                : [...prev, query]
        )
    }

    const fetchMetrics = async () => {
        const response = await getMetrics(selectedQueries)

        const data = response.data.data

        setRecentActivity(data.recentActivityFeed)
        setTop5UsersBibliokarma(data.usersKarmaTop5)
        setConversionRate(data.conversionRate)
        setRatingAnalysis(data.ratingAnalysis)
        setCatalogHealthStatus(data.catalogHealthStatus ?? null)
    }

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (menuRef.current && !menuRef.current.contains(event.target)) {
                setOpen(false)
            }
        }
        document.addEventListener("mousedown", handleClickOutside)
        return () => {
            document.removeEventListener("mousedown", handleClickOutside)
        }
    }, [])

    useEffect(() => {
        if (selectedQueries.length === 0) return

        fetchMetrics()
    }, [selectedQueries])

    return (
        <Stack className="bg-white min-h-screen w-7xl px-8 py-12 flex flex-col gap-6">
            <Box className="flex justify-between w-full p-2 stick border-gray-300">
                <Box>
                    <h1 className="text-4xl font-bold text-gray-900">Tablero de métricas</h1>
                    <p className="text-gray-500 mt-2">
                        Aquí vas a encontrar toda la información pertinente sobre la aplicación.
                    </p>
                </Box>
                <Box ref={menuRef}>
                    <div
                        onClick={() => setOpen(!open)}
                        className="p-4 rounded-full bg-blue-600 text-white flex items-center justify-center font-semibold cursor-pointer select-none hover:scale-105 hover:bg-blue-800 transition-transform duration-200"
                    >
                        Seleccionar tarjetas
                    </div>
                    {open && (
                        <div className="absolute mt-5 w-48 bg-white shadow-[0_10px_40px_rgba(0,0,0,0.25)] rounded-lg p-3 z-50">
                            <div className="text-sm font-medium text-gray-800 mb-3">
                                Queries disponibles
                            </div>
                            <FormGroup>
                                {queryOptions.map((query) => (
                                    <FormControlLabel
                                        key={query.id}
                                        label={query.label}
                                        control={
                                            <Checkbox
                                                checked={selectedQueries.includes(query.id)}
                                                onChange={() => handleCheckboxChange(query.id)}
                                            />
                                        }
                                    />
                                ))}
                            </FormGroup>
                        </div>
                    )}
                </Box>
            </Box>


            <Box sx={{
                display: "flex",
                flexWrap: "wrap",
                justifyContent: "center",
                mt: 3,
                gap: 2,
            }}>
                {selectedQueries.includes("activity") && (
                    <MetricCard title="Feed de actividad reciente">
                        <DynamicTable rows={recentActivity} />
                    </MetricCard>
                )}

                {selectedQueries.includes("karma") && (
                    <MetricCard title="Top 5 usuarios con más bibliokarmas">
                        <DynamicTable rows={top5UsersBibliokarma} />
                    </MetricCard>
                )}

                {selectedQueries.includes("conversion") && (
                    <MetricCard title="Tasa de conversión">
                        <DynamicTable rows={conversionRate} />
                    </MetricCard>
                )}

                {selectedQueries.includes("ratings") && (
                    <MetricCard title="Análisis de calificaciones">
                        <DynamicTable rows={ratingAnalysis} />
                    </MetricCard>
                )}

                {selectedQueries.includes("catalog") && (
                    <MetricCard title="Estado de salud del catálogo">
                        <DynamicTable rows={catalogHealthStatus} />
                    </MetricCard>
                )}
            </Box>
        </Stack>
    )
}

const MetricCard = ({ title, children }) => {
    return (
        <Box className="bg-gray-50 p-4 rounded-lg shadow-sm flex-1 w-256 border-1 border-r-3 border-b-3">
            <Typography variant="h6" fontWeight="bold" mb={2}>
                {title}
            </Typography>
            {children}
        </Box>
    )
}

export default KpiDashboard