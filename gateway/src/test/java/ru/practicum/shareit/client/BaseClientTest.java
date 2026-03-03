package ru.practicum.shareit.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.exception.ValidationException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BaseClientTest {

    private RestTemplate restTemplate;
    private TestClient testClient;
    private final String USER_ID_HEADER = "X-Sharer-User-Id";

    static class TestClient extends BaseClient {
        public TestClient(RestTemplate rest) {
            super(rest);
        }

        public ResponseEntity<Object> testGet(String path, Long userId, Map<String, Object> params) {
            return get(path, userId, params);
        }

        public ResponseEntity<Object> testPost(String path, Long userId, Object body) {
            return post(path, userId, body);
        }
    }

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        testClient = new TestClient(restTemplate);
    }

    @Test
    void testGetWithValidParams() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(Object.class), any(Map.class))).thenReturn(expectedResponse);

        ResponseEntity<Object> result = testClient.testGet("/test", 1L, Map.of("key", "value"));

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void testGetWithoutParams() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(Object.class))).thenReturn(expectedResponse);

        ResponseEntity<Object> result = testClient.testGet("/test", null, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void testPostWithValidData() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(Object.class))).thenReturn(expectedResponse);

        ResponseEntity<Object> result = testClient.testPost("/test", 1L, "body");

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void tetsMakeAndSendRequestWhenExceptionThrown() {
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exception.getResponseBodyAsByteArray()).thenReturn("Error".getBytes());

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(Object.class))).thenThrow(exception);

        ResponseEntity<Object> result = testClient.testGet("/test", 1L, null);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void tetsDefaultHeadersWithUserId() {
        // Тестируем через вызов метода, который использует headers
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(Object.class))).thenReturn(ResponseEntity.ok().build());

        testClient.testGet("/test", 1L, null);

        verify(restTemplate).exchange(anyString(), any(HttpMethod.class),
                argThat((HttpEntity<?> entity) -> {
                    HttpHeaders headers = entity.getHeaders();
                    return headers.containsKey(USER_ID_HEADER) &&
                            headers.getFirst(USER_ID_HEADER).equals("1");
                }), eq(Object.class));
    }

    @Test
    void testDefaultHeadersWithoutUserId() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(Object.class))).thenReturn(ResponseEntity.ok().build());

        testClient.testGet("/test", null, null);

        verify(restTemplate).exchange(anyString(), any(HttpMethod.class),
                argThat((HttpEntity<?> entity) -> {
                    HttpHeaders headers = entity.getHeaders();
                    return !headers.containsKey(USER_ID_HEADER);
                }), eq(Object.class));
    }
}