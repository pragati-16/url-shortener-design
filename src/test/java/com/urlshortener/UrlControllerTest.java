package com.urlshortener;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlController.class)
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private URLMapping urlMapping;

    // --- POST /shorten ---

    @Test
    void postShorten_validUrl_returns200WithShortCode() throws Exception {
        when(urlMapping.encode("https://example.com", null)).thenReturn("1");

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"originalUrl\":\"https://example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("1"))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/1"));
    }

    @Test
    void postShorten_withAlias_returnsAlias() throws Exception {
        when(urlMapping.encode("https://example.com", "myalias")).thenReturn("myalias");

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"originalUrl\":\"https://example.com\",\"alias\":\"myalias\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("myalias"));
    }

    @Test
    void postShorten_missingUrl_returns400() throws Exception {
        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postShorten_invalidUrl_returns400WithError() throws Exception {
        when(urlMapping.encode(any(), any()))
                .thenThrow(new IllegalArgumentException("URL must use http or https scheme"));

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"originalUrl\":\"ftp://example.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void postShorten_takenAlias_returns409WithError() throws Exception {
        when(urlMapping.encode(any(), any()))
                .thenThrow(new IllegalStateException("Alias 'x' is already taken by a different URL"));

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"originalUrl\":\"https://example.com\",\"alias\":\"x\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    // --- GET /{code} ---

    @Test
    void getCode_knownCode_returns301WithLocationHeader() throws Exception {
        when(urlMapping.decode("1")).thenReturn("https://example.com");

        mockMvc.perform(get("/1"))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", "https://example.com"));
    }

    @Test
    void getCode_unknownCode_returns404() throws Exception {
        when(urlMapping.decode("xyz")).thenReturn(null);

        mockMvc.perform(get("/xyz"))
                .andExpect(status().isNotFound());
    }
}
