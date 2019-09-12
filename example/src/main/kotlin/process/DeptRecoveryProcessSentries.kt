package io.holunda.talk.camundacon.process

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.springframework.stereotype.Component

/**
 * Evaluate sentry state based on current DeptRecoveryProcessData.
 */
interface DeptRecoveryProcessSentry {
  fun evaluate(execution: DelegateExecution): Boolean
}

// could be using DMN
@Component
class RemindViaTelephoneSentry(val om: ObjectMapper) : DeptRecoveryProcessSentry {
  override fun evaluate(execution: DelegateExecution): Boolean = with(execution.getProcessData(om)) {
    !helloLetterSent
      &&
      !deptPaid
      &&
      validPhone
      &&
      numberOfCallsThisDay <= 3
  }
}

// could be using DMN
@Component
class RemindViaMailSentry(val om: ObjectMapper) : DeptRecoveryProcessSentry {
  override fun evaluate(execution: DelegateExecution): Boolean = with(execution.getProcessData(om)) {
    !helloLetterSent
      &&
      !deptPaid
      &&
      validAddress
  }
}

 fun DelegateExecution.getProcessData(om: ObjectMapper): DeptRecoveryProcessData =
   om.readValue(this.getVariable(DeptRecoveryProcessData.KEY) as String)

