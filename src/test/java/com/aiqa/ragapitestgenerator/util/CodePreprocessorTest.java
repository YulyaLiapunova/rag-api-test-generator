package com.aiqa.ragapitestgenerator.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodePreprocessorTest {
    @Test
    void testPreprocessCode_RemovesBlockComments() {
        // Arrange
        String inputCode = "/* This is a block comment */\npublic class Test {}";

        // Act
        String result = CodePreprocessor.preprocessCode(inputCode);

        // Assert
        assertEquals("public class Test {}", result);
    }

    @Test
    void testPreprocessCode_RemovesLineComments() {
        // Arrange
        String inputCode = "// This is a line comment\npublic class Test {}";

        // Act
        String result = CodePreprocessor.preprocessCode(inputCode);

        // Assert
        assertEquals("public class Test {}", result);
    }

    @Test
    void testPreprocessCode_RemovesBothBlockAndLineComments() {
        // Arrange
        String inputCode = """
                /* Block comment */
                public class Test { // Line comment
                  /* Another block comment */
                  // Another line comment
                  public void method() {}
                }
                """;

        // Act
        String result = CodePreprocessor.preprocessCode(inputCode);

        // Assert
        assertEquals("public class Test { public void method() {} }", result);
    }

    @Test
    void testPreprocessCode_RemovesEmptyLinesAndTrims() {
        // Arrange
        String inputCode = """
                public class Test {
                
                  public void method() {
                  
                  }
                
                }
                """;

        // Act
        String result = CodePreprocessor.preprocessCode(inputCode);

        // Assert
        assertEquals("public class Test { public void method() { } }", result);
    }

    @Test
    void testPreprocessCode_HandlesEmptyInput() {
        // Arrange
        String inputCode = "";

        // Act
        String result = CodePreprocessor.preprocessCode(inputCode);

        // Assert
        assertEquals("", result);
    }

    @Test
    void testPreprocessCode_HandlesOnlyComments() {
        // Arrange
        String inputCode = """
                // Line comment
                /* Block comment */
                """;

        // Act
        String result = CodePreprocessor.preprocessCode(inputCode);

        // Assert
        assertEquals("", result);
    }

    @Test
    void testPreprocessCode_HandlesNoComments() {
        // Arrange
        String inputCode = "public class Test { public void method() {} }";

        // Act
        String result = CodePreprocessor.preprocessCode(inputCode);

        // Assert
        assertEquals("public class Test { public void method() {} }", result);
    }

    @Test
    void testPreprocessCode_HandlesComplexInput() {
        // Arrange
        String inputCode = """
                /* Block comment */
                public class Test { // Line comment
                  public void method() { 
                    // Inside method comment
                    int x = 10; /* Inline block comment */ 
                  }
                }
                """;

        // Act
        String result = CodePreprocessor.preprocessCode(inputCode);

        // Assert
        assertEquals("public class Test { public void method() { int x = 10; } }", result);
    }
}
