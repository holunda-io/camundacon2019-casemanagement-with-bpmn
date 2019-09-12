package io.holunda.talk.camundacon.process

import io.holunda.extension.casemanagement.bpmn.CaseProcessDefinition
import io.holunda.extension.casemanagement.persistence.BpmnCaseExecutionEntities
import io.swagger.annotations.ApiOperation
import org.camunda.bpm.engine.RuntimeService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping( "/dept", produces = [MediaType.APPLICATION_JSON_VALUE])
class DeptRecoveryProcessController(
  val process: DeptRecoveryProcessBean,
  val runtimeService: RuntimeService
) {

  /**
   * Start a new instance
   */
  @PostMapping("/start")
  @ApiOperation("start a new instance of DeptRecoveryProcess with given businessKey")
  fun start(
    @RequestParam("businessKey") businessKey: String
  ) : ResponseEntity<StartProcessDto> {

    val i = process.start(StartDeptRecoveryProcessCommand(businessKey))
    val data = process.runtimeService.getVariable(i.processInstanceId, DeptRecoveryProcessData.KEY) as DeptRecoveryProcessData
    return ResponseEntity.ok(StartProcessDto(businessKey, i.processInstanceId, data))
  }
  data class StartProcessDto(val businessKey: String, val processInstanceId: String, val data : DeptRecoveryProcessData)

  @PostMapping("/{businessKey}/validAddress")
  fun setValidAddress(
    @PathVariable("businessKey")  businessKey:String,
    @RequestParam("valid") valid : Boolean
  ) : ResponseEntity<Void> {
    val instance = process.findByBusinessKey(businessKey)
    if (!instance.isPresent) {return ResponseEntity.notFound().build()}

    val data : DeptRecoveryProcessData = (instance.get().runtimeService.getVariable(instance.get().processInstanceId, DeptRecoveryProcessData.KEY) as DeptRecoveryProcessData)
      .copy(validAddress = valid)

    instance.get().runtimeService.setVariable(instance.get().processInstanceId, DeptRecoveryProcessData.KEY,  data)


    return ResponseEntity.noContent().build()
  }

  /**
   * manually start a caseTask
   */
  @PostMapping("/{businessKey}/startTask/{caseTask}")
  fun startTask(
    @PathVariable("businessKey")  businessKey:String,
    @PathVariable("caseTask")  caseTask: DeptRecoveryProcessBean.CaseTasks
  ) : ResponseEntity<String> {
    val instance = process.findByBusinessKey(businessKey)
    if (!instance.isPresent) {return ResponseEntity.notFound().build()}

    val started = instance.get().startManually(caseTask.name)

    return if (started.isPresent()) ResponseEntity.ok(started.get()) else ResponseEntity.badRequest().build()
  }

  @ApiOperation("get execution state for given businessKey")
  @GetMapping("/{businessKey}/bpmnCaseExecutionEntities")
  fun getBpmnCaseExecutionEntities(@PathVariable("businessKey")  businessKey:String) : ResponseEntity<BpmnCaseExecutionEntities> {
    val instance = process.findByBusinessKey(businessKey)
    if (!instance.isPresent) {return ResponseEntity.notFound().build()}

    return ResponseEntity.ok(instance.get().bpmnCaseExecutionEntities)
  }

  @ApiOperation("get definitions state for given businessKey")
  @GetMapping("/{businessKey}/caseProcessDefinition")
  fun getCaseProcessDefinition(@PathVariable("businessKey")  businessKey:String) : ResponseEntity<CaseProcessDefinition> {
    val instance = process.findByBusinessKey(businessKey)
    if (!instance.isPresent) {return ResponseEntity.notFound().build()}

    return ResponseEntity.ok(instance.get().caseProcessDefinition)
  }

  @ApiOperation("pay dept")
  @GetMapping("/{businessKey}/payDept")
  fun payDept(@PathVariable("businessKey") businessKey: String)  : ResponseEntity<Void>{
    val instance = process.findByBusinessKey(businessKey)
    if (!instance.isPresent) {return ResponseEntity.notFound().build()}

    runtimeService.setVariable(instance.get().processInstanceId, DeptRecoveryProcessBean.VARIABLES.deptPaid, true)

    return ResponseEntity.ok().build()
  }
}
