package no.nav.soknad.archiving.joarkmock.repository

import no.nav.soknad.archiving.joarkmock.dto.JoarkDbData
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JoarkRepository: JpaRepository<JoarkDbData, String> {
	fun findByName(name: String): List<JoarkDbData>
}
