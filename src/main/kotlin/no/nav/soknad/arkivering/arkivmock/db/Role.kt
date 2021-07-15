package no.nav.soknad.arkivering.arkivmock.db

enum class Role {
	ADMIN, USER;

	override fun toString() = name.lowercase()
}
