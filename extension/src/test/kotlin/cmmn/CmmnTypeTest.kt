package io.holunda.extension.casemanagement.cmmn

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CmmnTypeTest {

  @Test
  fun `resolve humanTask`() {
    assertThat(CmmnType.byValue("humanTask")).isEqualTo(CmmnType.HUMAN_TASK)
  }
}