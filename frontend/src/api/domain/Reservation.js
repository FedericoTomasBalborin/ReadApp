export function mapToMyReservations(dto) {
    return {
        idBook: dto.idBook,
        coverBook: dto.coverBook,
        state: dto.state,
        title: dto.title,
        author: dto.author,
        ratingAverage: dto.ratingAverage,
        publisher: dto.publisher,
        idPublisher:dto.idPublisher,
        startDate: dto.startDate,
        endDate: dto.endDate,
        bibliokarma: dto.bibliokarma
    };
}