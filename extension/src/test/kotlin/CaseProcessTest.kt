package io.holunda.extension.cmmn

import io.holunda.extension.cmmn.command.StartProcessCommand
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.engine.variable.Variables
import org.junit.Rule
import org.junit.Test
import java.util.*

@Deployment(resources = ["DummyCaseProcess.bpmn"])
class CaseProcessTest {

  @get:Rule
  val camunda = ProcessEngineRule(CamundaTestConfiguration().buildProcessEngine())

  val process : DummyCaseProcess by lazy {
    DummyCaseProcess(camunda.runtimeService)
  }

  @Test
  fun `start process`() {
    val processInstance = process.start(DummyCaseProcess.Start())

    BpmnAwareTests.assertThat(processInstance.processInstance).isWaitingAt("keep_alive")
  }
}


class DummyCaseProcess(runtimeService: RuntimeService) : CaseProcess<DummyCaseProcess.Start, DummyCaseProcess.DummyCaseProcessInstance>(runtimeService) {
  override val key: String = "dummy_case_process"

  override fun wrap(processInstance: ProcessInstance): DummyCaseProcessInstance= DummyCaseProcessInstance(processInstance)


  data class Start(override val businessKey:String = UUID.randomUUID().toString()) : StartProcessCommand{
    override val variables = Variables.createVariables()!!
  }

  class DummyCaseProcessInstance(processInstance: ProcessInstance) : CaseProcessInstance(processInstance)

}


