package com.toneatti.catalogo;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Every error of the API is an application/problem+json body whose status matches the HTTP status
public final class ProblemDetailsMatchers {

    private ProblemDetailsMatchers() {
    }

    public static ResultMatcher problem(HttpStatus httpStatus) {
        return result -> {
            status().is(httpStatus.value()).match(result);
            content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON).match(result);
            jsonPath("$.status").value(httpStatus.value()).match(result);
        };
    }
}
