package ar.edu.unsam.phm.graphQL

import ar.edu.unsam.phm.dto.metrics.BookConversionsDTO
import ar.edu.unsam.phm.dto.metrics.CatalogHealthStatusDTO
import ar.edu.unsam.phm.dto.metrics.RatingByBookTypeDTO
import ar.edu.unsam.phm.dto.metrics.RecentActivityDTO
import ar.edu.unsam.phm.dto.metrics.UserKarmaDTO
import ar.edu.unsam.phm.services.MetricService
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize

@DgsComponent
@PreAuthorize("hasAuthority('Admin')")
class MetricResolver {

    @Autowired
    lateinit var metricService: MetricService

    @DgsQuery
    fun ratingAnalysis(): List<RatingByBookTypeDTO> =
        metricService.ratingAnalysis()

    @DgsQuery
    fun usersKarmaTop5(): List<UserKarmaDTO> =
        metricService.usersKarmaTop5()

    @DgsQuery
    fun conversionRate(): List<BookConversionsDTO> =
        metricService.conversionRate()

    @DgsQuery
    fun recentActivityFeed(): List<RecentActivityDTO> =
        metricService.recentActivityFeed()

    @DgsQuery
    fun catalogHealthStatus(): CatalogHealthStatusDTO =
        metricService.catalogHealthStatus()
}