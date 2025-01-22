package com.aiqa.ragapitestgenerator.util;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PatchParserTest {

    @Test
    void testParseChangedLines_NoChanges() {
        // Arrange
        String patch = """
            @@ -1,3 +1,3 @@
             unchanged line
             unchanged line
             unchanged line
        """;

        // Act
        List<Integer> changedLines = PatchParser.parseChangedLines(patch);

        // Assert
        assertTrue(changedLines.isEmpty());
    }

    @Test
    void testParseChangedLines_EmptyPatch() {
        // Arrange
        String patch = "";

        // Act
        List<Integer> changedLines = PatchParser.parseChangedLines(patch);

        // Assert
        assertTrue(changedLines.isEmpty());
    }

    @Test
    void testParseChangedLines_IgnoresLinesStartingWithPlusPlusPlus() {
        // Arrange
        String patch = """
        +++ new file
        @@ -1,3 +1,3 @@
         unchanged line
        +added line
        """;

        // Act
        List<Integer> changedLines = PatchParser.parseChangedLines(patch);

        // Assert
        assertEquals(1, changedLines.size());
        assertEquals(2, changedLines.get(0));
    }

    @Test
    void testParseChangedLines_DeletedLines() {
        // Arrange
        String patch = """
        @@ -1,3 +1,2 @@
        -removed line
         unchanged line
        +added line
        """;

        // Act
        List<Integer> changedLines = PatchParser.parseChangedLines(patch);

        // Assert
        assertEquals(2, changedLines.size(), "Changes 2 rows");
        assertEquals(1, changedLines.get(0), "First row");
        assertEquals(3, changedLines.get(1), "Second row");
    }
}
