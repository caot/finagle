package com.twitter.finagle.mysql

import com.twitter.finagle.Stack
import com.twitter.finagle.mysql.param.{Charset, Credentials, Database, FoundRows}
import org.scalatest.FunSuite

class HandshakeSettingsTest extends FunSuite {

  private val initial = Capability(
    Capability.Transactions,
    Capability.MultiResults
  )
  private val withFoundRows = initial + Capability.FoundRows

  test("HandshakeSettings adds FoundRows by default") {
    val settings = HandshakeSettings(clientCap = initial)
    assert(settings.calculatedClientCap == withFoundRows)
  }

  test("HandshakeSettings returns initial when found rows is disabled") {
    val settings = HandshakeSettings(clientCap = initial, enableFoundRows = false)
    assert(settings.calculatedClientCap == initial)
  }

  test("HandshakeSettings adds ConnectWithDB if database is defined") {
    val settings = HandshakeSettings(clientCap = initial, database = Some("test"))
    val withDB = withFoundRows + Capability.ConnectWithDB
    assert(settings.calculatedClientCap == withDB)
  }

  test("HandshakeSettings can calculate settings for SSL/TLS") {
    val settings = HandshakeSettings(clientCap = initial, database = Some("test"))
    val withDB = withFoundRows + Capability.ConnectWithDB
    val withSSL = withDB + Capability.SSL
    assert(settings.sslCalculatedClientCap == withSSL)
  }

  test("HandshakeSettings can read values from Stack params") {
    val params = Stack.Params.empty +
      Charset(MysqlCharset.Binary) +
      Credentials(Some("user123"), Some("pass123")) +
      Database(Some("test")) +
      FoundRows(false)
    val settings = HandshakeSettings(params)
    assert(settings.username == Some("user123"))
    assert(settings.password == Some("pass123"))
    assert(settings.database == Some("test"))
    assert(settings.charset == MysqlCharset.Binary)
    assert(!settings.enableFoundRows)
  }

}
