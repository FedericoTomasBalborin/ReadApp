package ar.edu.unsam.phm.bootstrap

import ar.edu.unsam.phm.domain.BOOK_GENRE
import ar.edu.unsam.phm.domain.BOOK_LANGUAGE
import ar.edu.unsam.phm.domain.BOOK_STATE
import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.BookClick
import ar.edu.unsam.phm.domain.CollectibleBook
import ar.edu.unsam.phm.domain.CommonBook
import ar.edu.unsam.phm.domain.DedicationBook
import ar.edu.unsam.phm.domain.Rating
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.USER_TYPE
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.domain.UserPublisher
import ar.edu.unsam.phm.repository.BookClickRepository
import ar.edu.unsam.phm.repository.BookRepository
import ar.edu.unsam.phm.repository.RatingRepository
import ar.edu.unsam.phm.repository.ReservationRepository
import ar.edu.unsam.phm.repository.UserRepository
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
@Profile("!test")
class Initializer(
    private val userRepository: UserRepository,
    private val ratingRepository: RatingRepository,
    private val reservationRepository: ReservationRepository,
    private val bookRepository: BookRepository,
    private val bookClickRepository: BookClickRepository,
) {
    private val passwordEncoder: PasswordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()


    // â”€â”€ Usuarios â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    val user1 = User(
        "Fran", "Demaino", "1234", "frandemaino@gmail.com",
        "4789075423", "Me gusta leer libros de ciencia ficciÃ³n y fantasÃ­a.",
        "Buenos Aires", mutableSetOf(USER_TYPE.READER, USER_TYPE.PUBLISHER), 2
    )
    val user2 = User(
        "Lucia", "Fernandez", "1234", "lucia.fernandez@gmail.com",
        "3921456723", "Disfruto escribir reseÃ±as de libros y descubrir nuevos autores.",
        "CÃ³rdoba", mutableSetOf(USER_TYPE.READER), 567
    )
    val user3 = User(
        "Mateo", "Gonzalez", "1234", "mateo.gonzalez@gmail.com",
        "4012398723", "Soy fanÃ¡tico de las novelas policiales y el misterio.",
        "Rosario", mutableSetOf(USER_TYPE.READER, USER_TYPE.PUBLISHER), 123
    )
    val user4 = User(
        "Valentina", "Rossi", "1234", "valentina.rossi@gmail.com",
        "4156789023", "Me encanta la literatura romÃ¡ntica y los clÃ¡sicos.",
        "Mendoza", mutableSetOf(USER_TYPE.PUBLISHER), 44
    )
    val user5 = User(
        "admin", "", "1234", "admin@gmail.com",
        "10000000", "Soy el dios de la aplicaciÃ³n!!!",
        "adminlandia", mutableSetOf(USER_TYPE.READER, USER_TYPE.PUBLISHER, USER_TYPE.ADMIN), 1000
    )

    // â”€â”€ Libros comunes â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    val commonBook1 = CommonBook(
        title = "Dune",
        description = "The messiah rises on Arrakis.",
        coverUrl = "https://m.media-amazon.com/images/I/41qSPS2EDdL._SY445_SX342_FMwebp_.jpg",
        author = "Frank Herbert",
        pages = 412,
        isbn = "9799876543211",
        language = BOOK_LANGUAGE.ENGLISH,
        editorial = "Ace",
        publicationDate = LocalDate.of(1965, 8, 1),
        state = BOOK_STATE.EXCELLENT,
        genre = BOOK_GENRE.SCIENCE_FICTION
    )

    val commonBook2 = CommonBook(
        title = "1984",
        description = "Dystopian novel about surveillance.",
        coverUrl = "https://images.cdn2.buscalibre.com/fit-in/360x360/3a/2c/3a2c227d11a1026b4aa3d45d33bad4f6.jpg",
        author = "George Orwell",
        pages = 328,
        isbn = "9799876543210",
        language = BOOK_LANGUAGE.ENGLISH,
        editorial = "Signet",
        publicationDate = LocalDate.of(1949, 6, 8),
        state = BOOK_STATE.BAD,
        genre = BOOK_GENRE.SCIENCE_FICTION
    )

    val commonBook3 = CommonBook(
        title = "El secreto",
        description = "Bueno",
        coverUrl = "https://www.iaavim.gob.ar/wp-content/uploads/2019/09/j8tqe0Xk8fbi8RvU5Bb1x1cOmgo.jpg",
        author = "Yo",
        pages = 152,
        isbn = "9791234567899",
        language = BOOK_LANGUAGE.ENGLISH,
        editorial = "Rivadavia",
        publicationDate = LocalDate.of(2026, 3, 15),
        state = BOOK_STATE.EXCELLENT,
        genre = BOOK_GENRE.SCIENCE_FICTION
    )

    val commonBook4 = CommonBook(
        title = "A storm of swords",
        description = "The mother of dragons is coming and the war of the five kings reach its climax.",
        coverUrl = "https://m.media-amazon.com/images/I/41-WGup6aSL._SY445_SX342_FMwebp_.jpg",
        author = "George RR Martin",
        pages = 973,
        isbn = "9791234567898",
        language = BOOK_LANGUAGE.ENGLISH,
        editorial = "Meisha Merlin",
        publicationDate = LocalDate.of(2000, 8, 8),
        state = BOOK_STATE.REGULAR,
        genre = BOOK_GENRE.DRAMA
    )

    val commonBook5 = CommonBook(
        title = "Kizumonogatari",
        description = "Boy meets a suicidal vampire.",
        coverUrl = "https://m.media-amazon.com/images/I/61i7bmQCa4L._SL1200_.jpg",
        author = "Nisio Isin",
        pages = 358,
        isbn = "9791234567897",
        language = BOOK_LANGUAGE.PORTUGUESE,
        editorial = "Vertical",
        publicationDate = LocalDate.of(2008, 5, 7),
        state = BOOK_STATE.GOOD,
        genre = BOOK_GENRE.ROMANCE
    )

    val commonBook6 = CommonBook(
        title = "Wuthering Heights",
        description = "A gothic tale of love, revenge and madness.",
        coverUrl = "https://m.media-amazon.com/images/I/41TjK8JiDcL._SY445_SX342_QL70_FMwebp_.jpg",
        author = "Emily Brontë",
        pages = 450,
        isbn = "9791234567896",
        language = BOOK_LANGUAGE.ENGLISH,
        editorial = "Penguin Classics",
        publicationDate = LocalDate.of(1847, 12, 1),
        state = BOOK_STATE.GOOD,
        genre = BOOK_GENRE.ROMANCE
    )

    val commonBook7 = CommonBook(
        title = "Design Patterns",
        description = "Elements of Reusable Object-Oriented Software.",
        coverUrl = "https://m.media-amazon.com/images/I/51nL96Abi1L._SY445_SX342_QL70_FMwebp_.jpg",
        author = "Erich Gamma",
        pages = 395,
        isbn = "9791234567895",
        language = BOOK_LANGUAGE.ENGLISH,
        editorial = "Adisson-Wesley",
        publicationDate = LocalDate.of(1994, 6, 8),
        state = BOOK_STATE.GOOD,
        genre = BOOK_GENRE.DESIGN
    )

    val commonBook8 = CommonBook(
        title = "And then there were none",
        description = "Ten strangers lured to an isolated Devon island by a mysterious host, U.N. Owen.",
        coverUrl = "https://m.media-amazon.com/images/I/41K-JpmjhHL._SY445_SX342_FMwebp_.jpg",
        author = "Agatha Christie",
        pages = 270,
        isbn = "9791234567894",
        language = BOOK_LANGUAGE.ENGLISH,
        editorial = "Collins Crime Club",
        publicationDate = LocalDate.of(1939, 11, 9),
        state = BOOK_STATE.BAD,
        genre = BOOK_GENRE.SCIENCE_FICTION
    )

    // â”€â”€ Libros con dedicatoria â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    val dedicationBook1 = DedicationBook(
        title = "A game of Thrones",
        description = "Winter is coming",
        coverUrl = "https://m.media-amazon.com/images/I/91L90Y-hpxL._SL1500_.jpg",
        author = "George RR Martin",
        pages = 700,
        isbn = "9791234567893",
        language = BOOK_LANGUAGE.PORTUGUESE,
        editorial = "Meisha Merlin",
        publicationDate = LocalDate.of(1996, 8, 1),
        state = BOOK_STATE.VERY_GOOD,
        genre = BOOK_GENRE.ROMANCE
    )

    val dedicationBook2 = DedicationBook(
        title = "Cadáver exquisito",
        description = "A lethal virus leaves humanity alone, leading to the legalization of cannibalism",
        coverUrl = "https://m.media-amazon.com/images/I/41u5fBR0aJL._SY445_SX342_FMwebp_.jpg",
        author = "Agustina Bazterrica",
        pages = 256,
        isbn = "9791234567892",
        language = BOOK_LANGUAGE.SPANISH,
        editorial = "Alfaguara",
        publicationDate = LocalDate.of(2017, 4, 12),
        state = BOOK_STATE.GOOD,
        genre = BOOK_GENRE.DRAMA
    )

    val dedicationBook3 = DedicationBook(
        title = "Frankestein",
        description = "Mad scientist creates life",
        coverUrl = "https://m.media-amazon.com/images/I/81MdZQFbwPL._SL1500_.jpg",
        author = "Mary Shelley",
        pages = 321,
        isbn = "9791234567891",
        language = BOOK_LANGUAGE.FRENCH,
        editorial = "e-artnow",
        publicationDate = LocalDate.of(1818, 1, 1),
        state = BOOK_STATE.VERY_GOOD,
        genre = BOOK_GENRE.CLASSIC_LITERATURE
    )

    val dedicationBook4 = DedicationBook(
        title = "Traumnovelle",
        description = "Doctor goes to a party uninvited",
        coverUrl = "https://m.media-amazon.com/images/I/51790T+XcsL._SY445_SX342_FMwebp_.jpg",
        author = "Arthur Schnitzler",
        pages = 127,
        isbn = "9791234567890",
        language = BOOK_LANGUAGE.ENGLISH,
        editorial = "Fischer Verlag",
        publicationDate = LocalDate.of(1926, 12, 25),
        state = BOOK_STATE.BAD,
        genre = BOOK_GENRE.SELF_HELP
    )

    val dedicationBook5 = DedicationBook(
        title = "The design of everyday things",
        description = "sum random art bs",
        coverUrl = "https://m.media-amazon.com/images/I/71sF8kuMW3L._SY466_.jpg",
        author = "Donald A. Norman",
        pages = 368,
        isbn = "9789876543211",
        language = BOOK_LANGUAGE.ENGLISH,
        editorial = "Basic Books",
        publicationDate = LocalDate.of(1988, 8, 1),
        state = BOOK_STATE.EXCELLENT,
        genre = BOOK_GENRE.DESIGN
    )

    val dedicationBook6 = DedicationBook(
        title = "Anne of the Green Gables",
        description = "Anne with an E if it was actually good",
        coverUrl = "https://m.media-amazon.com/images/I/81Z5oWTNdIL._SL1500_.jpg",
        author = "L. M. Montgomery",
        pages = 320,
        isbn = "9789876543210",
        language = BOOK_LANGUAGE.ENGLISH,
        editorial = "Signet",
        publicationDate = LocalDate.of(1908, 6, 25),
        state = BOOK_STATE.VERY_GOOD,
        genre = BOOK_GENRE.CLASSIC_LITERATURE
    )

    val dedicationBook7 = DedicationBook(
        title = "The Bible",
        description = "Source of irrefutable ultimate truth",
        coverUrl = "https://upload.wikimedia.org/wikipedia/commons/7/7c/Cima_da_Conegliano%2C_God_the_Father.jpg",
        author = "DIOS",
        pages = 1500,
        isbn = "9781234567899",
        language = BOOK_LANGUAGE.SPANISH,
        editorial = "Los amigos de Jesus",
        publicationDate = LocalDate.of(33, 12, 25),
        state = BOOK_STATE.EXCELLENT,
        genre = BOOK_GENRE.SELF_HELP
    )

    val dedicationBook8 = DedicationBook(
        title = "Bakemonogatari",
        description = "Suicidal vampire meets girl",
        coverUrl = "https://m.media-amazon.com/images/I/81ElK9889BL._SL1500_.jpg",
        author = "Nisio Isin",
        pages = 400,
        isbn = "9781234567898",
        language = BOOK_LANGUAGE.ENGLISH,
        editorial = "Vertical",
        publicationDate = LocalDate.of(2006, 11, 6),
        state = BOOK_STATE.GOOD,
        genre = BOOK_GENRE.ROMANCE
    )

    // â”€â”€ Libros coleccionables â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    val collectibleBook1 = CollectibleBook(
        title = "101 ways to make great friends",
        description = "Do you wanna be at the top of a Ponzi scheme?",
        coverUrl = "https://m.media-amazon.com/images/I/41yUhaMqyvL._SY445_SX342_FMwebp_.jpg",
        author = "Claire Quilty",
        pages = 101,
        isbn = "9781234567897",
        language = BOOK_LANGUAGE.PORTUGUESE,
        editorial = "Basic Books",
        publicationDate = LocalDate.of(2023, 12, 30),
        state = BOOK_STATE.EXCELLENT,
        genre = BOOK_GENRE.SELF_HELP
    )

    val collectibleBook2 = CollectibleBook(
        title = "Fargo: This Is a True Story",
        description = "What if you're right and they're wrong?",
        coverUrl = "https://m.media-amazon.com/images/I/A1oPs7hyH3L._SY385_.jpg",
        author = "Noah Hawley",
        pages = 448,
        isbn = "9781234567896",
        language = BOOK_LANGUAGE.ENGLISH,
        editorial = "Signet",
        publicationDate = LocalDate.of(2019, 10, 29),
        state = BOOK_STATE.EXCELLENT,
        genre = BOOK_GENRE.DRAMA
    )

    val collectibleBook3 = CollectibleBook(
        title = "Los ojos del perro siberiano",
        description = "Do you remember the last time we did talk?",
        coverUrl = "https://m.media-amazon.com/images/I/81zi3ZUDeQL._SL1500_.jpg",
        author = "Antonio Santa Ana",
        pages = 136,
        isbn = "9781234567895",
        language = BOOK_LANGUAGE.SPANISH,
        editorial = "Norma",
        publicationDate = LocalDate.of(2019, 4, 1),
        state = BOOK_STATE.EXCELLENT,
        genre = BOOK_GENRE.DRAMA
    )

    val collectibleBook4 = CollectibleBook(
        title = "The 120 days of Sodom",
        description = "MOST CONTROVERSIAL NOVEL IN HUMAN HISTORY",
        coverUrl = "https://m.media-amazon.com/images/I/71lqXm20a2L._SY522_.jpg",
        author = "Marquis de Sade",
        pages = 400,
        isbn = "9781234567894",
        language = BOOK_LANGUAGE.FRENCH,
        editorial = "Grapevine",
        publicationDate = LocalDate.of(1904, 12, 30),
        state = BOOK_STATE.BAD,
        genre = BOOK_GENRE.CLASSIC_LITERATURE
    )

    val collectibleBook5 = CollectibleBook(
        title = "Do Androids Dream of Electric Sheep?",
        description = "I've seen things you people wouldn't believe. Attack ships on fire off the shoulder of Orion. I watched C-beams glitter in the dark near the TannhÃ¤user Gate. All those moments will be lost in time, like tears in rain. Time to die.",
        coverUrl = "https://m.media-amazon.com/images/I/912qCxdgTRL._SL1500_.jpg",
        author = "Philip K. Dick",
        pages = 230,
        isbn = "9781234567893",
        language = BOOK_LANGUAGE.PORTUGUESE,
        editorial = "Grapevine",
        publicationDate = LocalDate.of(1968, 2, 13),
        state = BOOK_STATE.VERY_GOOD,
        genre = BOOK_GENRE.SCIENCE_FICTION
    )

    val collectibleBook6 = CollectibleBook(
        title = "The Castle",
        description = "The haunting tale of K.'s relentless, unavailing struggle with an inscrutable authority in order to gain access to the Castle.",
        coverUrl = "https://m.media-amazon.com/images/I/61Nsxdo2p3L._SY466_.jpg",
        author = "Frank Kafka",
        pages = 358,
        isbn = "9781234567892",
        language = BOOK_LANGUAGE.SPANISH,
        editorial = "Grapevine",
        publicationDate = LocalDate.of(1998, 12, 15),
        state = BOOK_STATE.EXCELLENT,
        genre = BOOK_GENRE.CLASSIC_LITERATURE
    )

    val collectibleBook7 = CollectibleBook(
        title = "From Russia with Love",
        description = "James Bond's best movie's book",
        coverUrl = "https://m.media-amazon.com/images/I/71Sz7sF4chL._SL1500_.jpg",
        author = "Ian Fleming",
        pages = 200,
        isbn = "9781234567891",
        language = BOOK_LANGUAGE.PORTUGUESE,
        editorial = "Gatiss",
        publicationDate = LocalDate.of(2025, 1, 26),
        state = BOOK_STATE.REGULAR,
        genre = BOOK_GENRE.ROMANCE
    )

    val collectibleBook8 = CollectibleBook(
        title = "Dragon Ball Z vol. 17",
        description = "Cell reaches his perfect form",
        coverUrl = "https://m.media-amazon.com/images/I/81q5jUdqJGL._SY466_.jpg",
        author = "Akira Toriyama",
        pages = 190,
        isbn = "9781234567890",
        language = BOOK_LANGUAGE.SPANISH,
        editorial = "IVREA",
        publicationDate = LocalDate.of(2004, 10, 12),
        state = BOOK_STATE.VERY_GOOD,
        genre = BOOK_GENRE.SCIENCE_FICTION
    )

    // Helpers

    private fun addBooks() {
        // Asignar userPublisher a todos los libros (después de que los usuarios tengan IDs)
        commonBook1.userPublisher = UserPublisher(user3.id!!, user3.name)
        commonBook2.userPublisher = UserPublisher(user3.id!!, user3.name)
        commonBook3.userPublisher = UserPublisher(user3.id!!, user3.name)
        commonBook4.userPublisher = UserPublisher(user3.id!!, user3.name)
        commonBook5.userPublisher = UserPublisher(user3.id!!, user3.name)
        commonBook6.userPublisher = UserPublisher(user3.id!!, user3.name)
        commonBook7.userPublisher = UserPublisher(user4.id!!, user4.name)
        commonBook8.userPublisher = UserPublisher(user3.id!!, user3.name)

        dedicationBook1.userPublisher = UserPublisher(user4.id!!, user4.name)
        dedicationBook2.userPublisher = UserPublisher(user4.id!!, user4.name)
        dedicationBook3.userPublisher = UserPublisher(user4.id!!, user4.name)
        dedicationBook4.userPublisher = UserPublisher(user1.id!!, user1.name)
        dedicationBook5.userPublisher = UserPublisher(user3.id!!, user3.name)
        dedicationBook6.userPublisher = UserPublisher(user4.id!!, user4.name)
        dedicationBook7.userPublisher = UserPublisher(user1.id!!, user1.name)
        dedicationBook8.userPublisher = UserPublisher(user3.id!!, user3.name)

        collectibleBook1.userPublisher = UserPublisher(user1.id!!, user1.name)
        collectibleBook2.userPublisher = UserPublisher(user4.id!!, user4.name)
        collectibleBook3.userPublisher = UserPublisher(user4.id!!, user4.name)
        collectibleBook4.userPublisher = UserPublisher(user3.id!!, user3.name)
        collectibleBook5.userPublisher = UserPublisher(user4.id!!, user4.name)
        collectibleBook6.userPublisher = UserPublisher(user4.id!!, user4.name)
        collectibleBook7.userPublisher = UserPublisher(user4.id!!, user4.name)
        collectibleBook8.userPublisher = UserPublisher(user4.id!!, user4.name)

        val books = mutableListOf<Book>(
            commonBook1, commonBook2, commonBook3, commonBook4,
            commonBook5, commonBook6, commonBook7, commonBook8,
            dedicationBook1, dedicationBook2, dedicationBook3, dedicationBook4,
            dedicationBook5, dedicationBook6, dedicationBook7, dedicationBook8,
            collectibleBook1, collectibleBook2, collectibleBook3, collectibleBook4,
            collectibleBook5, collectibleBook6, collectibleBook7, collectibleBook8
        )
        books.forEach { bookRepository.save(it) }

        bookClickRepository.saveAll(
            listOf(
                BookClick(commonBook1.id, user1.name),
                BookClick(commonBook1.id, user1.name),
                BookClick(commonBook2.id, user2.name),
                BookClick(commonBook2.id, user2.name),
                BookClick(commonBook2.id, user2.name),
                BookClick(commonBook3.id, user3.name),
                BookClick(commonBook3.id, user3.name),
                BookClick(commonBook3.id, user3.name),
                BookClick(commonBook4.id, user4.name),
                BookClick(commonBook4.id, user4.name),
                BookClick(commonBook4.id, user4.name),
                BookClick(commonBook5.id, user5.name),
                BookClick(commonBook5.id, user5.name),
            )
        )
    }

    private fun addUsers() {
        listOf(user1, user2, user3, user4, user5).forEach {
            it.password = passwordEncoder.encode(it.password)
            userRepository.save(it)
        }
    }

    private fun addReservations() {
        val reservations = listOf(
            Reservation(user = user5, startDate = LocalDate.of(2026, 3, 21), endDate = LocalDate.of(2026, 3, 21), book = commonBook1),
            Reservation(user = user5, startDate = LocalDate.of(2026, 3, 22), endDate = LocalDate.of(2026, 3, 24), book = commonBook2),
            Reservation(user = user2, startDate = LocalDate.of(2026, 4, 10), endDate = LocalDate.of(2026, 4, 24), book = commonBook3),
            Reservation(user = user2, startDate = LocalDate.of(2026, 5, 10), endDate = LocalDate.of(2026, 5, 24), book = commonBook2),
            Reservation(user = user5, startDate = LocalDate.of(2026, 6, 22), endDate = LocalDate.of(2026, 6, 24), book = commonBook4),
            Reservation(user = user5, startDate = LocalDate.of(2026, 7, 22), endDate = LocalDate.of(2026, 7, 24), book = commonBook5),
            Reservation(user = user5, startDate = LocalDate.of(2026, 8, 22), endDate = LocalDate.of(2026, 8, 24), book = commonBook6),
            Reservation(user = user1, startDate = LocalDate.of(2026, 4, 1), endDate = LocalDate.of(2026, 4, 7), book = commonBook7),
            Reservation(user = user1, startDate = LocalDate.of(2026, 5, 15), endDate = LocalDate.of(2026, 5, 20), book = dedicationBook4),
            Reservation(user = user3, startDate = LocalDate.of(2026, 4, 5), endDate = LocalDate.of(2026, 4, 12), book = collectibleBook4),
            Reservation(user = user3, startDate = LocalDate.of(2026, 6, 1), endDate = LocalDate.of(2026, 6, 10), book = dedicationBook5),
            Reservation(user = user4, startDate = LocalDate.of(2026, 5, 5), endDate = LocalDate.of(2026, 5, 9), book = commonBook4),
            Reservation(user = user4, startDate = LocalDate.of(2026, 7, 10), endDate = LocalDate.of(2026, 7, 15), book = collectibleBook2)
        )
        reservations.forEach { reservationRepository.save(it) }

        // Mantiene en Mongo la cantidad de reservas por libro para búsquedas rápidas.
        commonBook1.addReservation(reservations[0])
        commonBook2.addReservation(reservations[1])
        commonBook3.addReservation(reservations[2])
        commonBook2.addReservation(reservations[3])
        commonBook4.addReservation(reservations[4])
        commonBook5.addReservation(reservations[5])
        commonBook6.addReservation(reservations[6])
        commonBook7.addReservation(reservations[7])
        dedicationBook4.addReservation(reservations[8])
        collectibleBook4.addReservation(reservations[9])
        dedicationBook5.addReservation(reservations[10])
        commonBook4.addReservation(reservations[11])
        collectibleBook2.addReservation(reservations[12])
        bookRepository.saveAll(
            listOf(
                commonBook1, commonBook2, commonBook3, commonBook4,
                commonBook5, commonBook6, commonBook7,
                dedicationBook4, dedicationBook5,
                collectibleBook2, collectibleBook4
            )
        )
    }

    private fun addRatings() {
        val ratings = listOf(
            Rating(user = user1, calification = 5.0, comment = "Excelente libro", bookId = commonBook1.id),
            Rating(user = user2, calification = 2.0, comment = "Aburrido", bookId = commonBook1.id),
            Rating(user = user3, calification = 2.5, comment = "Maso menos", bookId = commonBook1.id)
        )
        ratings.forEach { ratingRepository.save(it) }

        commonBook1.ratingCount = ratings.size
        commonBook1.calification = ratings.map { it.calification }.average()
        bookRepository.save(commonBook1)
    }

    private fun existsEmptyTable(): Boolean =
        userRepository.count() == 0L
        || ratingRepository.count() == 0L
        || reservationRepository.count() == 0L
        || bookRepository.count() == 0L

    private fun deleteAllData() {
        userRepository.deleteAll()
        ratingRepository.deleteAll()
        reservationRepository.deleteAll()
        bookRepository.deleteAll()
    }

    // â”€â”€ Init â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional
    @PostConstruct
    fun init() {
        if (existsEmptyTable()) {
            deleteAllData()
            println("Inicializando datos...")
            addUsers()
            addBooks()
            addReservations()
            addRatings()

            // SINCRONIZAR currentlyBorrowed
            syncCurrentlyBorrowed()
        } else {
            println("La base de datos ya estaba inicializada")
        }
    }

    private fun syncCurrentlyBorrowed() {
        val today = LocalDate.now()
        val allBooks = bookRepository.findAll()

        allBooks.forEach { book ->
            book.currentlyBorrowed = book.reservationDates.any { dateRange ->
                dateRange.from <= today && dateRange.to >= today
            }
        }

        bookRepository.saveAll(allBooks)
        println("Sincronización completada: ${allBooks.size} libros actualizados")
    }
}
