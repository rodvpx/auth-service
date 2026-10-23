package com.backendlab.authservice.controller;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminControllerTest {

    private final AdminController controller = new AdminController();

    @Test
    void whenAdminTestIsCalled_thenReturnsAdminMessage() {
        assertThat(controller.adminTest()).isEqualTo("Área ADMIN 🔥");
    }
}
