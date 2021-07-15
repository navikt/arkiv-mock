package no.nav.soknad.arkivering.arkivmock.db

import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

interface DatabaseInterface {
	val connection: Connection
	val dataSource: HikariDataSource
}

