package io.holunda.extension.casemanagement

import _test.DummyCaseProcess
import _test.Start
import cmmn.BpmnCaseExecutionState
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.extension.casemanagement.cmmn.RepetitionRule
import io.holunda.extension.casemanagement.persistence.BpmCaseExecutionRepositoryFactory
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.extension.mockito.CamundaMockito
import org.junit.Rule
import org.junit.Test

@Deployment(resources = [DummyCaseProcess.BPMN])
class CaseProcessTest {

  private val om = jacksonObjectMapper()

  @get:Rule
  val camunda = ProcessEngineRule(CamundaTestConfiguration().buildProcessEngine()).apply {
    BpmnAwareTests.init(this.processEngine)
  }

  val process: DummyCaseProcess by lazy {
    DummyCaseProcess(camunda.runtimeService, camunda.repositoryService).apply {
      CamundaMockito.registerInstance(this)
    }
  }

  val repositoryFactory = BpmCaseExecutionRepositoryFactory(om)

  @Test
  fun `start process`() {
    val processInstance = process.start(Start())

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("keep_alive")

    val def = processInstance.caseProcessDefinition
    assertThat(def.tasks[DummyCaseProcess.CaseTasks.runAutomatically_repetitionNone.key]!!.repetitionRule).isEqualTo(RepetitionRule.NONE)

    assertThat(processInstance.bpmnCaseExecutionEntities.executions).isNotEmpty()

    val enabled = processInstance.findExecutions(state= BpmnCaseExecutionState.ENABLED)

    println("enabled: $enabled")
    println("all: ${processInstance.findExecutions()}")


    //val repository = repositoryFactory.create(camunda.runtimeService, processInstance.id)

  }


}


