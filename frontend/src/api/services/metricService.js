import { api } from "../apiClient"
import { URL_SERVIDOR_REST } from "../../utils/configuration"
import { buildMetricsQuery } from "../../utils/graphqlQueries"

const getMetrics = async (selectedQueries) => {
    const queries = buildMetricsQuery(selectedQueries)
    return await api.post(
        `${URL_SERVIDOR_REST}/graphql`,
        {
            query: queries
        }
    )
}

export default getMetrics