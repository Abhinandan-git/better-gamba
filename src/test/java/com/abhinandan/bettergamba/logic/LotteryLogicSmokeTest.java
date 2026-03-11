package com.abhinandan.bettergamba.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke test — verifies the JUnit 5 + ModDevGradle test harness is wired correctly.
 *
 * @see LotteryLogic
 */
class LotteryLogicSmokeTest {
    @Test
    void testHarnessIsAlive() {
        // If this test runs, JUnit 5 + ModDevGradle unitTest wiring is correct.
        assertTrue(true, "JUnit 5 harness is operational");
    }
}
