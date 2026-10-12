package com.skillgap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Basic smoke and sanity tests for StudentSkillGapApplication.
 */
class StudentSkillGapApplicationTests {

    @Test
    @DisplayName("Verify that the application class is loaded and instantiable")
    void applicationClassLoads() {
        StudentSkillGapApplication app = new StudentSkillGapApplication();
        assertNotNull(app, "StudentSkillGapApplication instance must not be null");
    }
}
