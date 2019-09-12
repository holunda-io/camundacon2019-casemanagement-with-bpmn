package io.holunda.talk.camundacon.process

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
class RemindViaTelephoneSentry : DeptRecoveryProcessSentry {
  override fun evaluate(execution: DelegateExecution): Boolean = with(execution.getProcessData()) {
    helloLetterSent
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
class RemindViaMailSentry : DeptRecoveryProcessSentry {
  override fun evaluate(execution: DelegateExecution): Boolean = with(execution.getProcessData()) {
    helloLetterSent
      &&
      !deptPaid
      &&
      validAddress
  }
}

 fun DelegateExecution.getProcessData(): DeptRecoveryProcessData = this.getVariable(DeptRecoveryProcessData.KEY) as DeptRecoveryProcessData

