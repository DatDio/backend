package com.mailshop_dragonvu.utils;

import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.util.Strings;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays;

@UtilityClass
public class EnumParseUtils {

    /**
     * Parse comma-separated string values to enum set
     * @param input Comma-separated string (e.g., "1,2,3")
     * @param parser Function to parse individual value
     * @return Set of parsed enum values, empty set if input is null/blank
     */
    public static <T> Set<T> parseEnumSet(String input, 
                                          java.util.function.Function<String, T> parser) {
        if (Strings.isBlank(input)) {
            return Collections.emptySet();
        }

        return Arrays.stream(input.split(","))
                .map(String::trim)
                .map(parser)
                .collect(Collectors.toSet());
    }

    /**
     * Parse comma-separated integer strings to enum set using fromKey method
     * @param input Comma-separated string (e.g., "1,0")
     * @param fromKeyMethod Method reference that converts Integer to Enum (e.g., ActiveStatusEnum::fromKey)
     * @return Set of parsed enum values
     */
    public static <T> Set<T> parseEnumSetByKey(String input, 
                                               java.util.function.Function<Integer, T> fromKeyMethod) {
        if (Strings.isBlank(input)) {
            return Collections.emptySet();
        }

        return Arrays.stream(input.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .map(fromKeyMethod)
                .collect(Collectors.toSet());
    }
}
