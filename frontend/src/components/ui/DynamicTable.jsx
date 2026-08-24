import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow
} from "@mui/material"

const renderValue = (value) => {
    if (value === null || value === undefined) {
        return "-"
    }

    if (typeof value === "object") {
        return JSON.stringify(value)
    }

    return String(value)
}

const flattenObject = (obj, prefix = "") => {
    return Object.keys(obj).reduce((acc, key) => {
        const value = obj[key]
        const newKey = prefix ? `${prefix}_${key}` : key

        if (
            value &&
            typeof value === "object" &&
            !Array.isArray(value)
        ) {
            Object.assign(acc, flattenObject(value, newKey))
        } else {
            acc[newKey] = value
        }

        return acc
    }, {})
}

const DynamicTable = ({ rows }) => {
    const normalizedRows = Array.isArray(rows)
        ? rows
        : rows
            ? [rows]
            : []

    if (normalizedRows.length === 0) {
        return <div>No hay datos disponibles para renderizar esta visual</div>
    }

    const flattenedRows = normalizedRows.map(row => flattenObject(row))

    const columns = Object.keys(flattenedRows[0])

    return (
        <Table className="border-2">
            <TableHead>
                <TableRow className="bg-yellow-300">
                    {columns.map(column => (
                        <TableCell className="border-1" key={column} align="center">
                            {column}
                        </TableCell>
                    ))}
                </TableRow>
            </TableHead>

            <TableBody>
                {flattenedRows.map((row, index) => (
                    <TableRow key={index}>
                        {columns.map(column => (
                            <TableCell className="cursor-pointer border-1 hover:bg-gray-200" key={column}>
                                {renderValue(row[column])}
                            </TableCell>
                        ))}
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    )
}

export default DynamicTable