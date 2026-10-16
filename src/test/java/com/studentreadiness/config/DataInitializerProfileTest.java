package com.studentreadiness.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DataInitializerProfileTest {

    @Test
    void demoDataInitializerIsRestrictedToDevProfile() {
        Profile profile = DataInitializer.class.getAnnotation(Profile.class);

        assertNotNull(profile);
        assertArrayEquals(new String[]{"dev"}, profile.value());
    }
}
