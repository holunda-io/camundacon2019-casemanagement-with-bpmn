package io.holunda.talk.camundacon.process

import java.util.*

/**
 * Representing all data for the dept recovery case
 *
 * In real life, this would be solved via ID as variable and dynamic loading of some persisted life data.
 */
data class DeptRecoveryProcessData(
  val uuid : String = UUID.randomUUID().toString(),
  val validAddress : Boolean = false,
  val validPhone : Boolean = false,
  val numberOfCallsThisDay : Int = 0,
  val deptPaid : Boolean = false,
  val helloLetterSent : Boolean = false
) {
  companion object {
    const val KEY = "processData"
  }
}
