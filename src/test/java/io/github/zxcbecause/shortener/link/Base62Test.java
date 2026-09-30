package io.github.zxcbecause.shortener.link;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Base62Test {

    @Test
    void encodesKnownValues() {
        assertThat(Base62.encode(0)).isEqualTo("0");
        assertThat(Base62.encode(61)).isEqualTo("Z");
        assertThat(Base62.encode(62)).isEqualTo("10");
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 42, 3843, 1_000_000, Long.MAX_VALUE})
    void decodeIsInverseOfEncode(long value) {
        assertThat(Base62.decode(Base62.encode(value))).isEqualTo(value);
    }

    @Test
    void rejectsInvalidInput() {
        assertThatThrownBy(() -> Base62.encode(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Base62.decode("abc!")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Base62.decode("")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void randomCodeHasRequestedLengthAndAlphabet() {
        String code = Base62.random(10, new Random(1));
        assertThat(code).hasSize(10).matches("[0-9a-zA-Z]+");
    }
}
