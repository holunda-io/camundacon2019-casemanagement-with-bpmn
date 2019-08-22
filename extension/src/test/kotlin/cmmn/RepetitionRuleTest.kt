package io.holunda.extension.casemanagement.cmmn

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RepetitionRuleTest {

  @Test
  fun `null for null`() {
    assertThat(RepetitionRule.byValue(null)).isNull()
  }

  @Test
  fun `none for none`() {
    assertThat(RepetitionRule.byValue("none")).isEqualTo(RepetitionRule.NONE)
  }

  @Test
  fun `complete for complete`() {
    assertThat(RepetitionRule.byValue("complete")).isEqualTo(RepetitionRule.COMPLETE)
  }


}