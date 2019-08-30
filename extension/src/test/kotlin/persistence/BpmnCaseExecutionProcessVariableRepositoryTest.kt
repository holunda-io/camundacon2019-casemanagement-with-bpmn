package io.holunda.extension.casemanagement.persistence

import cmmn.BpmnCaseExecutionState
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.*
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.atomic.AtomicReference

class BpmnCaseExecutionProcessVariableRepositoryTest {

  private val repository = BpmnCaseExecutionProcessVariableRepository(
      executions = mutableListOf(),
      commit = {}
  )

  @Test
  fun `repository can save new execution`() {
    assertThat(repository.count()).isEqualTo(0)

    val entity = BpmnCaseExecutionEntity("foo")
    assertThat(entity.transient).isTrue()

    val saved = repository.save(entity)
    assertThat(saved.transient).isFalse()

    assertThat(repository.count()).isEqualTo(1)
  }

  @Test
  fun `an existing entity is modified on save`() {
    val existing = repository.save(BpmnCaseExecutionEntity("foo"))

    val modified = existing.copy(state = BpmnCaseExecutionState.AVAILABLE)
    val saved = repository.save(modified)

    val all = repository.findAll()

    assertThat(all).hasSize(1)
    assertThat(all.get(0)).isEqualTo(saved)
  }

  @Test
  fun `a non transient entity can not be saved if its id does not exist`() {
    val fakeEntity = BpmnCaseExecutionEntity(id = "1", caseTaskKey = "foo")

    assertThatThrownBy { repository.save(fakeEntity) }
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("entity with id=1 does not exist")
  }

  @Test
  fun `find by id`() {
    val existing = repository.save(BpmnCaseExecutionEntity("foo"))

    assertThat(repository.findById("non-existing")).isNull()
    assertThat(repository.findById(existing.id!!)).isEqualTo(existing)
  }

  @Test
  fun `find by caseTaskKey`() {
    val f1 = repository.save(BpmnCaseExecutionEntity("foo"))
    val b1 = repository.save(BpmnCaseExecutionEntity("bar"))
    val b2 = repository.save(BpmnCaseExecutionEntity("bar"))

    val list = repository.findByCaseTaskKey("bar")

    assertThat(list).containsExactlyInAnyOrder(b1,b2)
  }



  @Test
  fun `repository can commit to store`() {
    // prepare a repo that stores to atomic reference
    val om = jacksonObjectMapper()
    val store = AtomicReference<String>()

    val storingRepository = BpmnCaseExecutionProcessVariableRepository(
        executions = mutableListOf(),
        commit = {
      store.set(om.writeValueAsString(BpmnCaseExecutionEntities(it)))
    }
    )

    assertThat(store.get()).isNull()

    // save an execution and ensure it can be found
    val e = storingRepository.save(BpmnCaseExecutionEntity("foo"))
    assertThat(storingRepository.findById(e.id!!)).isEqualTo(e)

    // commit stores json in ref
    storingRepository.commit()
    assertThat(store.get()).isNotNull()

    // create a new instance of repo based on executions from store
    val storedExecutions = om.readValue<BpmnCaseExecutionEntities>(store.get())
    val repoCopy = BpmnCaseExecutionProcessVariableRepository(storedExecutions.executions.toMutableList(), {})

    // it has one element
    assertThat(repoCopy.count()).isEqualTo(1)

    // this element is e
    assertThat(repoCopy.findAll().first()).isEqualTo(e)
    assertThat(repoCopy.findAll().first().id).isEqualTo(e.id)

    // and we can find it
    assertThat(repoCopy.findById(e.id!!)).isNotNull().isEqualTo(e)
  }
}