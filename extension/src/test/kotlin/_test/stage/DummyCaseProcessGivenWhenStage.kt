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
import io.holunda.extension.casemanagement.ActivityId
import io.holunda.extension.casemanagement._test.JGivenKotlinStage
import io.holunda.extension.casemanagement._test.step
import io.holunda.extension.casemanagement.findTask
import io.holunda.extension.casemanagement.persistence.BpmCaseExecutionRepositoryFactory
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.extension.mockito.CamundaMockito
import java.util.*

@JGivenKotlinStage
@Suppress("UNUSED")
class DummyCaseProcessGivenWhenStage : Stage<DummyCaseProcessGivenWhenStage>() {

  @ExpectedScenarioState
  private lateinit var camunda: ProcessEngineRule

  @ProvidedScenarioState
  private lateinit var processInstance: DummyCaseProcessInstance

  @ProvidedScenarioState
  private val objectMapper = jacksonObjectMapper()

  @ProvidedScenarioState
  private val startedTasks: MutableMap<CaseTask, MutableList<Optional<String>>> = mutableMapOf()

  @ProvidedScenarioState
  private val repositoryFactory = BpmCaseExecutionRepositoryFactory(objectMapper)

  private val manualStartRepetitionCompleteWithSentryResolver = ManualStartRepetitionCompleteWithSentryResolver()

  private val process: DummyCaseProcess by lazy {
    DummyCaseProcess(camunda.runtimeService, camunda.repositoryService).apply {
      CamundaMockito.registerInstance(this)

      CamundaMockito.registerInstance("manualStartRepetitionCompleteWithSentryResolver", manualStartRepetitionCompleteWithSentryResolver)
    }
  }

  fun `the case process is started`(@Quoted cmd: Start = Start()) = step {
    processInstance = process.start(cmd)
    BpmnAwareTests.assertThat(processInstance).isWaitingAt("keep_alive")
  }

  fun `a caseTask $ is manually started`(@Quoted caseTask: CaseTask) = step {
    startedTasks.putIfAbsent(caseTask, mutableListOf())
    startedTasks[caseTask]!!.add(processInstance.startManually(caseTask))
  }

  fun `the sentry for task $ evaluates to $`(@Quoted caseTask: CaseTask, @Quoted sentryCondition: Boolean) = step {
    when (caseTask) {
      CaseTask.manualStart_repetitionComplete_withSentry -> manualStartRepetitionCompleteWithSentryResolver.state = sentryCondition
      else -> throw UnsupportedOperationException("no sentry defined for task $caseTask")
    }
  }

  fun `the userTask $ is completed`(userTask: ActivityId) = step {
    val task = processInstance.findTask(userTask.key, camunda.taskService) ?: throw IllegalStateException("no userTask found for ${userTask.key}")

    camunda.taskService.complete(task.id)
  }

  fun `sentry re-evaluation is triggered`() = step {
    processInstance.triggerSentryReevaluation()
  }
}
