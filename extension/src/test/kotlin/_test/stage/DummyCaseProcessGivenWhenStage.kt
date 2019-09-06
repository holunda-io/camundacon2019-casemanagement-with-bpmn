package io.holunda.extension.casemanagement._test.stage

import _test.DummyCaseProcess
import _test.DummyCaseProcess.CaseTask
import _test.DummyCaseProcessInstance
import _test.ManualStartRepetitionCompleteWithSentryResolver
import _test.Start
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.ExpectedScenarioState
import com.tngtech.jgiven.annotation.ProvidedScenarioState
import com.tngtech.jgiven.annotation.Quoted
import io.holunda.extension.casemanagement.CaseProcessTest
import io.holunda.extension.casemanagement._test.JGivenKotlinStage
import io.holunda.extension.casemanagement.persistence.BpmCaseExecutionRepositoryFactory
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.extension.mockito.CamundaMockito
import java.util.concurrent.locks.Condition

@JGivenKotlinStage
class DummyCaseProcessGivenWhenStage : Stage<DummyCaseProcessGivenWhenStage>() {

  @ExpectedScenarioState
  lateinit var camunda: ProcessEngineRule

  @ProvidedScenarioState
  lateinit var processInstance:DummyCaseProcessInstance

  @ProvidedScenarioState
  val objectMapper = jacksonObjectMapper()

  @ProvidedScenarioState
  val repositoryFactory = BpmCaseExecutionRepositoryFactory(objectMapper)

  val manualStartRepetitionCompleteWithSentryResolver = ManualStartRepetitionCompleteWithSentryResolver()

  val process: DummyCaseProcess by lazy {
    DummyCaseProcess(camunda.runtimeService, camunda.repositoryService).apply {
      CamundaMockito.registerInstance(this)
      CamundaMockito.registerInstance("expressionSpike", CaseProcessTest.ExpressionSpikeListener())
      CamundaMockito.registerInstance("sentry", CaseProcessTest.SentryResolver())

      CamundaMockito.registerInstance("manualStartRepetitionCompleteWithSentryResolver", manualStartRepetitionCompleteWithSentryResolver)
    }
  }


  fun `the case process is started`(@Quoted cmd: Start = Start()): DummyCaseProcessGivenWhenStage {
    processInstance = process.start(cmd)
    BpmnAwareTests.assertThat(processInstance).isWaitingAt("keep_alive")

    return self()
  }

  fun `the sentry for task $ evaluates to $`(@Quoted caseTask: CaseTask, @Quoted sentryCondition:  Boolean) = self().apply {
    when(caseTask) {
      CaseTask.manualStart_repetitionComplete_withSentry -> manualStartRepetitionCompleteWithSentryResolver.state = sentryCondition
      else -> throw UnsupportedOperationException("no sentry defined for task $caseTask")
    }
  }
}
