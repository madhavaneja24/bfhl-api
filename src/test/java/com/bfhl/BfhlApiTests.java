package com.bfhl;

import com.bfhl.dto.BfhlRequest;
import com.bfhl.dto.BfhlResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BfhlApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ── Helper ────────────────────────────────────────────────────────────────

    private BfhlResponse postBfhl(List<String> data) throws Exception {
        BfhlRequest req = new BfhlRequest(data);
        MvcResult result = mockMvc.perform(post("/bfhl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), BfhlResponse.class);
    }

    // ── Example A ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Example A: mixed numbers, letters, special char")
    void testExampleA() throws Exception {
        BfhlResponse resp = postBfhl(Arrays.asList("a", "1", "334", "4", "R", "$"));

        assertThat(resp.isSuccess()).isTrue();
        assertThat(resp.getUserId()).isNotBlank();

        assertThat(resp.getOddNumbers()).containsExactlyInAnyOrder("1");
        assertThat(resp.getEvenNumbers()).containsExactlyInAnyOrder("334", "4");
        assertThat(resp.getAlphabets()).containsExactlyInAnyOrder("A", "R");
        assertThat(resp.getSpecialCharacters()).containsExactlyInAnyOrder("$");
        assertThat(resp.getSum()).isEqualTo("339");

        // concat: letters in order → "a","R" → "aR", reversed "Ra", alt-caps → "Ra"
        assertThat(resp.getConcatString()).isEqualTo("Ra");
    }

    // ── Example B ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Example B: multiple numbers, letters, special chars")
    void testExampleB() throws Exception {
        BfhlResponse resp = postBfhl(Arrays.asList("2", "a", "y", "4", "&", "-", "*", "5", "92", "b"));

        assertThat(resp.isSuccess()).isTrue();
        assertThat(resp.getOddNumbers()).containsExactlyInAnyOrder("5");
        assertThat(resp.getEvenNumbers()).containsExactlyInAnyOrder("2", "4", "92");
        assertThat(resp.getAlphabets()).containsExactlyInAnyOrder("A", "Y", "B");
        assertThat(resp.getSpecialCharacters()).containsExactlyInAnyOrder("&", "-", "*");
        assertThat(resp.getSum()).isEqualTo("103");
        // letters in order: a,y,b → "ayb", reversed "bya", alt-caps → "ByA"
        assertThat(resp.getConcatString()).isEqualTo("ByA");
    }

    // ── Example C ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Example C: only multi-char alphabetic tokens")
    void testExampleC() throws Exception {
        BfhlResponse resp = postBfhl(Arrays.asList("A", "ABCD", "DOE"));

        assertThat(resp.isSuccess()).isTrue();
        assertThat(resp.getOddNumbers()).isEmpty();
        assertThat(resp.getEvenNumbers()).isEmpty();
        assertThat(resp.getAlphabets()).containsExactlyInAnyOrder("A", "ABCD", "DOE");
        assertThat(resp.getSpecialCharacters()).isEmpty();
        assertThat(resp.getSum()).isEqualTo("0");
        // letters in order: A,A,B,C,D,D,O,E → "AABCDDoe" reversed → "EoDdCbAa"
        // alt-caps on "EoDdCbAa" → E(upper)o(lower)D(upper)d(lower)C(upper)b(lower)A(upper)a(lower)
        // = "EoDdCbAa"
        assertThat(resp.getConcatString()).isEqualTo("EoDdCbAa");
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Empty data array returns success with empty lists and sum 0")
    void testEmptyArray() throws Exception {
        BfhlResponse resp = postBfhl(Collections.emptyList());

        assertThat(resp.isSuccess()).isTrue();
        assertThat(resp.getOddNumbers()).isEmpty();
        assertThat(resp.getEvenNumbers()).isEmpty();
        assertThat(resp.getAlphabets()).isEmpty();
        assertThat(resp.getSpecialCharacters()).isEmpty();
        assertThat(resp.getSum()).isEqualTo("0");
        assertThat(resp.getConcatString()).isEmpty();
    }

    @Test
    @DisplayName("Only numbers: even/odd split and correct sum")
    void testOnlyNumbers() throws Exception {
        BfhlResponse resp = postBfhl(Arrays.asList("3", "6", "11", "100"));

        assertThat(resp.getOddNumbers()).containsExactlyInAnyOrder("3", "11");
        assertThat(resp.getEvenNumbers()).containsExactlyInAnyOrder("6", "100");
        assertThat(resp.getAlphabets()).isEmpty();
        assertThat(resp.getSpecialCharacters()).isEmpty();
        assertThat(resp.getSum()).isEqualTo("120");
        assertThat(resp.getConcatString()).isEmpty();
    }

    @Test
    @DisplayName("Only special characters: no numbers or letters")
    void testOnlySpecialChars() throws Exception {
        BfhlResponse resp = postBfhl(Arrays.asList("@", "#", "!", "%"));

        assertThat(resp.isSuccess()).isTrue();
        assertThat(resp.getOddNumbers()).isEmpty();
        assertThat(resp.getEvenNumbers()).isEmpty();
        assertThat(resp.getAlphabets()).isEmpty();
        assertThat(resp.getSpecialCharacters()).containsExactlyInAnyOrder("@", "#", "!", "%");
        assertThat(resp.getSum()).isEqualTo("0");
    }

    @Test
    @DisplayName("Response contains user_id, email, roll_number")
    void testResponseMetadata() throws Exception {
        BfhlResponse resp = postBfhl(List.of("1"));

        assertThat(resp.getUserId()).isNotBlank();
        assertThat(resp.getEmail()).isNotBlank();
        assertThat(resp.getRollNumber()).isNotBlank();
    }

    @Test
    @DisplayName("Alphabets are returned in uppercase")
    void testAlphabetsUpperCase() throws Exception {
        BfhlResponse resp = postBfhl(Arrays.asList("abc", "xyz", "Hello"));

        // "Hello" has lowercase letters → it's NOT purely alphabetic only if H is,
        // actually "Hello" IS all letters, so it should be uppercased.
        assertThat(resp.getAlphabets()).allMatch(s -> s.equals(s.toUpperCase()));
    }

    @Test
    @DisplayName("sum is returned as String, not number")
    void testSumIsString() throws Exception {
        String raw = mockMvc.perform(post("/bfhl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"data\":[\"5\",\"10\"]}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // "sum" value must be a JSON string (surrounded by quotes)
        assertThat(raw).containsPattern("\"sum\"\\s*:\\s*\"15\"");
    }

    @Test
    @DisplayName("Missing data field returns 400 Bad Request")
    void testMissingDataField() throws Exception {
        mockMvc.perform(post("/bfhl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.is_success").value(false));
    }

    @Test
    @DisplayName("Malformed JSON returns 400 Bad Request")
    void testMalformedJson() throws Exception {
        mockMvc.perform(post("/bfhl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.is_success").value(false));
    }

    @Test
    @DisplayName("Single letter: concat_string equals that letter in uppercase")
    void testSingleLetter() throws Exception {
        BfhlResponse resp = postBfhl(List.of("z"));
        // reversed single char = "z", alt-caps index 0 → upper → "Z"
        assertThat(resp.getConcatString()).isEqualTo("Z");
    }

    @Test
    @DisplayName("Zero is treated as even number")
    void testZeroIsEven() throws Exception {
        BfhlResponse resp = postBfhl(List.of("0"));
        assertThat(resp.getEvenNumbers()).containsExactly("0");
        assertThat(resp.getOddNumbers()).isEmpty();
        assertThat(resp.getSum()).isEqualTo("0");
    }
}
