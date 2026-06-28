package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DemoApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void contextLoads() {
	}

	@Test
	void createAndRedirectTracksAnalytics() throws Exception {
		String payload = objectMapper.writeValueAsString(new UrlRequest("https://example.com/path", "alpha1234", null));

		mockMvc.perform(post("/api/v1/urls")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code").value("alpha1234"));

		mockMvc.perform(get("/alpha1234"))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", "https://example.com/path"));

		mockMvc.perform(get("/api/v1/urls/alpha1234/analytics"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalClicks").value(1));
	}

	@Test
	void duplicateCustomAliasReturnsConflict() throws Exception {
		String payload = objectMapper.writeValueAsString(new UrlRequest("https://example.com/one", "dupe1234", null));

		mockMvc.perform(post("/api/v1/urls")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/v1/urls")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isConflict());
	}

	@Test
	void localhostUrlIsRejected() throws Exception {
		String payload = objectMapper.writeValueAsString(new UrlRequest("http://localhost:8080/internal", "safe1234", null));

		mockMvc.perform(post("/api/v1/urls")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isBadRequest());
	}

	private record UrlRequest(String url, String customAlias, String expiresAt) {
	}

}
