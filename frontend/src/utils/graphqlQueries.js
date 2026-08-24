export const buildMetricsQuery = (selectedQueries) => {
    const queryMap = {
        activity: `
            recentActivityFeed {
                date
                eventType
                user {
                    name
                    email
                }
            }
        `,

        karma: `
            usersKarmaTop5 {
                username
                bibliokarma
            }
        `,

        conversion: `
            conversionRate {
                idBook
                clickCount
                reservationCount
                conversionRate
            }
        `,

        ratings: `
            ratingAnalysis {
                bookType
                averageRating
            }
        `,

        catalog: `
            catalogHealthStatus {
                neverBeenReserved
                reservedToday
                availableWithFutureReservations
                availableWithPastReservation
                total
            }
        `
    }

    const fields = selectedQueries
        .map(query => queryMap[query])
        .join("\n")

    return `
        query {
            ${fields}
        }
    `
}