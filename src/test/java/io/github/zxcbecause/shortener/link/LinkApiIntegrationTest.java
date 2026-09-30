package io.github.zxcbecause.shortener.link;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "shortener.base-url=https://sho.rt")
@AutoConfigureMockMvc
@Transactional
class LinkApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    private void shorten(String json) throws Exception {
        mvc.perform(post("/api/links").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void shortenRedirectAndCountClicks() throws Exception {
        mvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\": \"https://github.com/zxcbecause\", \"alias\": \"gh\"}"))
                .andExpect(status().isBadRequest()); // alias too short

        shorten("{\"url\": \"https://github.com/zxcbecause\", \"alias\": \"my-gh\"}");

        mvc.perform(get("/my-gh"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://github.com/zxcbecause"));
        mvc.perform(get("/my-gh")).andExpect(status().isFound());

        mvc.perform(get("/api/links/my-gh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").value("https://sho.rt/my-gh"))
                .andExpect(jsonPath("$.clicks").value(2))
                .andExpect(jsonPath("$.lastAccessedAt").isNotEmpty());
    }

    @Test
    void generatedCodeHasConfiguredLength() throws Exception {
        mvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\": \"https://example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.matchesPattern("[0-9a-zA-Z]{7}")));
    }

    @Test
    void duplicateAliasReturns409() throws Exception {
        shorten("{\"url\": \"https://example.com\", \"alias\": \"dup\"}");

        mvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\": \"https://example.org\", \"alias\": \"dup\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Alias taken"));
    }

    @Test
    void invalidUrlReturns400() throws Exception {
        mvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\": \"javascript:alert(1)\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid URL"));
    }

    @Test
    void deletedLinkReturns404() throws Exception {
        shorten("{\"url\": \"https://example.com\", \"alias\": \"temp\"}");

        mvc.perform(delete("/api/links/temp")).andExpect(status().isNoContent());
        mvc.perform(get("/temp")).andExpect(status().isNotFound());
    }
}
