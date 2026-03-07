package enterprises.iwakura.docs.utils;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import enterprises.iwakura.docs.util.StringUtils;

public class StringUtilsTest {

    private static Stream<Arguments> isSimilarArguments() {
        return Stream.of(
            // Identical strings
            Arguments.of("voile", "voile", true),
            Arguments.of("hello", "hello", true),

            // Case-insensitive comparison
            Arguments.of("Hello", "hello", true),
            Arguments.of("WORLD", "world", true),
            Arguments.of("VoIlE", "voile", true),

            // Very similar strings (small typos)
            Arguments.of("voile", "voila", true),
            Arguments.of("colour", "color", true),

            // Completely different strings
            Arguments.of("apple", "zebra", false),
            Arguments.of("cat", "elephant", false),

            // Null inputs
            Arguments.of(null, "hello", false),
            Arguments.of("hello", null, false),
            Arguments.of(null, null, false),

            // Empty strings
            Arguments.of("", "", true),
            Arguments.of("hello", "", false),
            Arguments.of("", "hello", false),

            // Single character strings
            Arguments.of("a", "a", true),
            Arguments.of("a", "b", false),

            // Prefix similarity (Winkler bonus)
            Arguments.of("internet", "internal", true),
            Arguments.of("prefix", "prefixes", true),

            // Transpositions
            Arguments.of("abcde", "abdce", true),

            // Slightly different lengths
            Arguments.of("test", "tests", true),
            Arguments.of("testing", "testign", true),

            // Various mod names - exact matches
            Arguments.of("BetterMap", "BetterMap", true),
            Arguments.of("EyeSpy", "EyeSpy", true),
            Arguments.of("Overstacked", "Overstacked", true),
            Arguments.of("Spellbook", "Spellbook", true),
            Arguments.of("Hytalor", "Hytalor", true),
            Arguments.of("Perfect Parries - Souls-Like Parrying", "Perfect Parries - Souls-Like Parrying", true),

            // Various mod names - case insensitive
            Arguments.of("bettermap", "BetterMap", true),
            Arguments.of("EYESPY", "EyeSpy", true),
            Arguments.of("overstacked", "Overstacked", true),
            Arguments.of("spellbook", "Spellbook", true),

            // Various mod names - typos / slight variations
            Arguments.of("BetterMapp", "BetterMap", true),
            Arguments.of("EyeSp", "EyeSpy", true),
            Arguments.of("Overstackd", "Overstacked", true),
            Arguments.of("Spellbok", "Spellbook", true),
            Arguments.of("Vein Minin", "Vein Mining", true),
            Arguments.of("Lucky Minin", "Lucky Mining", true),
            Arguments.of("Simple Enchantment", "Simple Enchantments", true),
            Arguments.of("Violet Furnishings", "Violet's Furnishings", true),
            Arguments.of("Perfect Parries", "Perfect Parries - Souls-Like Parrying", true),
            Arguments.of("Perfect Parry", "Perfect Parries - Souls-Like Parrying", false), // Too different

            // Various mod names - completely different
            Arguments.of("BetterMap", "Spellbook", false),
            Arguments.of("EyeSpy", "Overstacked", false),
            Arguments.of("Hytalor", "Simply Trash", false),
            Arguments.of("Lucky Mining", "Vein Mining", false),
            Arguments.of("MMO Skill Tree", "Simple Enchantments", false),

            // Various mod names - prefix bonus
            Arguments.of("Simple Enchantments", "Simply Trash", false),
            Arguments.of("MultipleHUD", "Multiple", true),
            Arguments.of("Hybrid", "Hytalor", false)
        );
    }

    @ParameterizedTest
    @MethodSource("isSimilarArguments")
    public void testIsSimilar(String first, String second, boolean result) {
        Assertions.assertEquals(result, StringUtils.isSimilar(first, second));
    }
}
