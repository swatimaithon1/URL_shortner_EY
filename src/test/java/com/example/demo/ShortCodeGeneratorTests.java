package com.example.demo;

import com.example.demo.urlshortener.service.ShortCodeGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShortCodeGeneratorTests {

    @Test
    void generatedCodeUsesRequestedLength() {
        ShortCodeGenerator generator = new ShortCodeGenerator();

        String code = generator.generate(8);

        assertThat(code).hasSize(8);
        assertThat(code).matches("^[a-zA-Z0-9]{8}$");
    }
}

