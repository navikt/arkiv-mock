package no.nav.soknad.arkivering.arkivmock.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(code = HttpStatus.GONE, reason = "Mocked exception")
class GoneException: Exception()

