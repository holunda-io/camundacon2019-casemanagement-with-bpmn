@file:Suppress("unused")
package io.holunda.extension.casemanagement._test

// TODO replace with jgiven-addons, see #23

import com.google.common.reflect.TypeToken
import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.base.ScenarioTestBase
import com.tngtech.jgiven.impl.Scenario
import com.tngtech.jgiven.junit.JGivenClassRule
import com.tngtech.jgiven.junit.JGivenMethodRule
import org.junit.ClassRule
import org.junit.Rule

/**
 * Use `GIVEN` instead of `given()` on Given-Stage "G"
 */
val <G : Stage<G>, W : Stage<W>, T : Stage<T>> ScenarioTestBase<G, W, T>.GIVEN: G get() = given()

/**
 * Use `WHEN` instead of `when()` on When-Stage "W"
 */
val <G : Stage<G>, W : Stage<W>, T : Stage<T>> ScenarioTestBase<G, W, T>.WHEN: W get() = `when`()

/**
 * Use `THEN` instead of `then()` on Then-Stage "T"
 */
val <G : Stage<G>, W : Stage<W>, T : Stage<T>> ScenarioTestBase<G, W, T>.THEN: T get() = then()

/**
 * Use `AND` instead of `and()`
 */
val <X : Stage<X>> Stage<X>.AND: X get() = and()

/**
 * Use `WITH` instead of `with()`
 */
val <X : Stage<X>> Stage<X>.WITH: X get() = with()

/**
 * Use `BUT` instead of `but()`
 */
val <X : Stage<X>> Stage<X>.BUT: X get() = but()


val <X : Stage<X>> Stage<X>.self: X get() = self()!!


/**
 * Marker annotation for all-open compiler plugin.
 */
annotation class JGivenKotlinStage

abstract class DualScenarioTestBase<GIVEN_WHEN, THEN> : ScenarioTestBase<GIVEN_WHEN, GIVEN_WHEN, THEN>() {

  @Suppress("UNCHECKED_CAST")
  override fun createScenario(): Scenario<GIVEN_WHEN, GIVEN_WHEN, THEN> {
    val givenWhenClass = object : TypeToken<GIVEN_WHEN>(javaClass) {}.rawType as Class<GIVEN_WHEN>
    val thenClass = object : TypeToken<THEN>(javaClass) {}.rawType as Class<THEN>

    return Scenario(givenWhenClass, givenWhenClass, thenClass)
  }

}

open class DualScenarioTest<GIVEN_WHEN, THEN> : DualScenarioTestBase<GIVEN_WHEN, THEN>() {

  companion object {
    @get: ClassRule
    @JvmStatic
    val writerRule = JGivenClassRule()
  }

  @Suppress("UNCHECKED_CAST")
  override fun getScenario(): Scenario<GIVEN_WHEN, GIVEN_WHEN, THEN> = scenarioRule.scenario as Scenario<GIVEN_WHEN, GIVEN_WHEN, THEN>


  @get: Rule
  val scenarioRule = JGivenMethodRule(createScenario())


}
