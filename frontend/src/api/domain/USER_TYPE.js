export const USER_TYPE = {
    READER:    { name: "Lector" },
    PUBLISHER: { name: "Publicador" },
    ADMIN:     { name: "Admin" },
}

export const getTypeFromName = (name) => {
    return Object.values(USER_TYPE).find(t => t.name === name)?.id || null
}