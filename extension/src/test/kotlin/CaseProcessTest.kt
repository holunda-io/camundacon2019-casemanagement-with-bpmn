package io.holunda.extension.casemanagement

import io.holunda.extension.casemanagement.cmmn.RepetitionRule
import io.holunda.extension.cmmn.command.StartProcessCommand
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.extension.mockito.CamundaMockito
import org.junit.Rule
import org.junit.Test
import java.util.*

@Deployment(resources = ["DummyCaseProcess.bpmn"])
class CaseProcessTest {

  @get:Rule
  val camunda = ProcessEngineRule(CamundaTestConfiguration().buildProcessEngine()).apply {
    BpmnAwareTests.init(this.processEngine)
  }

  val process: DummyCaseProcess by lazy {
    DummyCaseProcess(camunda.runtimeService, camunda.repositoryService).apply {
      CamundaMockito.registerInstance(this)
    }
  }

  @Test
  fun `start process`() {
    val processInstance = process.start(DummyCaseProcess.Start())

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("keep_alive")

    val def = processInstance.caseProcessDefinition

    assertThat(def.tasks["runAutomatically_repetitionNone"]!!.repetitionRule).isEqualTo(RepetitionRule.NONE)
  }
}


class DummyCaseProcess(
    runtimeService: RuntimeService,
    repositoryService: RepositoryService
) : CaseProcess<DummyCaseProcess.Start, DummyCaseProcess.DummyCaseProcessInstance>(
    runtimeService,
    repositoryService
) {
  override val key: String = "dummy_case_process"

  override fun wrap(processInstance: ProcessInstance): DummyCaseProcessInstance = DummyCaseProcessInstance(processInstance, runtimeService)

  data class Start(override val businessKey: String = UUID.randomUUID().toString()) : StartProcessCommand {
    override val variables = Variables.createVariables()!!
  }

  class DummyCaseProcessInstance(processInstance: ProcessInstance, runtimeService: RuntimeService) : CaseProcessInstance(processInstance, runtimeService)

}


