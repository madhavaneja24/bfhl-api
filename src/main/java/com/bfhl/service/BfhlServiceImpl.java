package com.bfhl.service;

import com.bfhl.dto.BfhlRequest;
import com.bfhl.dto.BfhlResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class BfhlServiceImpl implements BfhlService {

    // ── User constants – change these to your own details ──────────────────
    private static final String FULL_NAME  = "Madhav_Aneja";   // lowercase, underscore-separated
    private static final String DOB        = "24022005";   // ddmmyyyy
    private static final String EMAIL      = "madhav1971.be23@chitkara.edu.in";
    private static final String ROLL_NUMBER = "2310991971";
    // ───────────────────────────────────────────────────────────────────────

    private static final String USER_ID = FULL_NAME + "_" + DOB;

    @Override
    public BfhlResponse processData(BfhlRequest request) {

        List<String> data = request.getData();

        List<String> evenNumbers      = new ArrayList<>();
        List<String> oddNumbers       = new ArrayList<>();
        List<String> alphabets        = new ArrayList<>();
        List<String> specialChars     = new ArrayList<>();
        long         numericSum       = 0;
        StringBuilder lettersInOrder  = new StringBuilder(); // raw letter chars, in input order

        for (String token : data) {
            if (token == null || token.isEmpty()) {
                specialChars.add(token != null ? token : "");
                continue;
            }

            if (isNumeric(token)) {
                // Even / odd classification
                long value = Long.parseLong(token);
                numericSum += value;
                if (value % 2 == 0) {
                    evenNumbers.add(token);
                } else {
                    oddNumbers.add(token);
                }

            } else if (isAlphabetic(token)) {
                // Collect individual letters for concat logic
                for (char c : token.toCharArray()) {
                    lettersInOrder.append(c);
                }
                alphabets.add(token.toUpperCase());

            } else {
                // Mixed or contains special characters
                specialChars.add(token);
            }
        }

        String concatString = buildConcatString(lettersInOrder.toString());

        return BfhlResponse.builder()
                .isSuccess(true)
                .userId(USER_ID)
                .email(EMAIL)
                .rollNumber(ROLL_NUMBER)
                .oddNumbers(oddNumbers)
                .evenNumbers(evenNumbers)
                .alphabets(alphabets)
                .specialCharacters(specialChars)
                .sum(String.valueOf(numericSum))
                .concatString(concatString)
                .build();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Returns true if every character in the token is a digit.
     */
    private boolean isNumeric(String token) {
        for (char c : token.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    /**
     * Returns true if every character in the token is a letter.
     */
    private boolean isAlphabetic(String token) {
        for (char c : token.toCharArray()) {
            if (!Character.isLetter(c)) return false;
        }
        return true;
    }

    /**
     * Builds the concat_string:
     *  1. Take all letter characters collected from the input in order.
     *  2. Reverse the entire sequence.
     *  3. Apply alternating caps starting with UPPER at index 0.
     *
     * Example: input letters "a y b"  → reversed "byA" with alt-caps → "ByA"
     *   index 0 → upper ('B'), index 1 → lower ('y'), index 2 → upper ('A')
     */
    private String buildConcatString(String letters) {
        if (letters.isEmpty()) return "";

        // Step 1: reverse
        String reversed = new StringBuilder(letters).reverse().toString();

        // Step 2: alternate caps (UPPER at even indices, lower at odd)
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < reversed.length(); i++) {
            char c = reversed.charAt(i);
            if (i % 2 == 0) {
                result.append(Character.toUpperCase(c));
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }
}
