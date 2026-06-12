package br.com.frotasPro.api.controller.handlers;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import br.com.frotasPro.api.shared.exception.ConflictException;
import br.com.frotasPro.api.shared.exception.ControllerExceptionHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ControllerExceptionHandlerTest {

    @Test
    void retornaConflictParaEixoComVinculos() {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/eixo/123");
        var response = new ControllerExceptionHandler().conflict(
                new ConflictException("Não é possível excluir o eixo porque ele possui vínculos."),
                request
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Não é possível excluir o eixo porque ele possui vínculos.", response.getBody().getError());
    }
}
