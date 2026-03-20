package serviceTests;
import static org.junit.jupiter.api.Assertions.*;

import client.services.LanguageService;
import client.services.MarkdownService;
import org.junit.jupiter.api.Test;

public class MarkdownServiceTest {

    @Test
    void normalMarkdownTextTest() {
        MarkdownService markdownService = new MarkdownService(new LanguageService());
        String markdown = "this is normal text.";
        String html = markdownService.convertMarkdownToHtml(markdown);

        assertTrue(html.contains("this is normal text."));
        assertFalse(html.contains("<h1>"), "No header tags should be added to plain text.");
    }

    @Test
    void convertMarkdownToHtmlTest() {
        MarkdownService markdownService = new MarkdownService(new LanguageService());
        String markdown = "This is aaaaaaaaaaaaaaaaaaa veryyyyy long line that should be wrapped.";

        String html = markdownService.convertMarkdownToHtml(markdown);

        //tests for line wrapping
        assertTrue(html.contains("word-wrap: break-word;"));
        assertTrue(html.contains("white-space: pre-wrap;"));

        assertTrue(html.contains("This is aaaaaaaaaaaaaaaaaaa veryyyyy long line that should be wrapped."));
    }

    @Test
    void complexMarkdownToHtmlTest() {
        MarkdownService markdownService = new MarkdownService(new LanguageService());
        String markdown = """
            ### Heading
            **Bold** and *italic*.

            - Bullet 1
            - Bullet 2
            
            ```
            block with long a LONG long long long long ultra long line that should wrap.
            ```
            """;

        String htmlMarkdown = markdownService.convertMarkdownToHtml(markdown);

        assertTrue(htmlMarkdown.contains("<h3>Heading</h3>"));
        assertTrue(htmlMarkdown.contains("<strong>Bold</strong>"));
        assertTrue(htmlMarkdown.contains("<ul>"));
        assertTrue(htmlMarkdown.contains("<pre>"));
        assertTrue(htmlMarkdown.contains("word-wrap: break-word;"));
    }

    @Test
    void emptyMarkdownTest() {
        MarkdownService markdownService = new MarkdownService(new LanguageService());

        String markdown = "";
        String html = markdownService.convertMarkdownToHtml(markdown);

        assertNotNull(html, "HTML should not be null");
        assertTrue(html.contains("<body></body>"));
    }

    @Test
    void longWordMarkdownTest() {
        MarkdownService markdownService = new MarkdownService(new LanguageService());

        String markdown = "llllllllllllllllllllllllllll" +
                "ooooooooooooooooooooooooooooooo" +
                "nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn" +
                "gggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg";
        String html = markdownService.convertMarkdownToHtml(markdown);

        assertTrue(html.contains("word-wrap: break-word;"));
    }

    @Test
    void paragraphsMarkdownTest() {
        MarkdownService markdownService = new MarkdownService(new LanguageService());

        String markdown = """
        This is the first paragraph.

        This is the second paragraph.
        """;

        String html = markdownService.convertMarkdownToHtml(markdown);

        //both paragraphs should be marked as paragraphs in the html
        assertTrue(html.contains("<p>This is the first paragraph.</p>"));
        assertTrue(html.contains("<p>This is the second paragraph.</p>"));
    }

    @Test
    void markdownHeaderConvertTest() {
        MarkdownService markdownService = new MarkdownService(new LanguageService());
        String markdown = """
        # Header 1
        ## Header 2
        ### Header 3
        #### Header 4
        ##### Header 5
        ###### Header 6
        """;

        String html = markdownService.convertMarkdownToHtml(markdown);

        //Each header should be specifically marked in the html
        assertTrue(html.contains("<h1>Header 1</h1>"));
        assertTrue(html.contains("<h2>Header 2</h2>"));
        assertTrue(html.contains("<h3>Header 3</h3>"));
        assertTrue(html.contains("<h4>Header 4</h4>"));
        assertTrue(html.contains("<h5>Header 5</h5>"));
        assertTrue(html.contains("<h6>Header 6</h6>"));
    }

    @Test
    void emptyHeaderTest() {
        MarkdownService markdownService = new MarkdownService(new LanguageService());
        String markdown = "# ";
        String html = markdownService.convertMarkdownToHtml(markdown);

        //there should be nothing between the formatting signs
        assertTrue(html.contains("<h1></h1>"));
    }

    @Test
    void testBoldAndItalicCombinedMarkdown() {
        MarkdownService markdownService = new MarkdownService(new LanguageService());

        String markdown = "***Bold italic***";
        String html = markdownService.convertMarkdownToHtml(markdown);

        assertTrue(html.contains("<em><strong>Bold italic</strong></em>"));

    }

    @Test
    void markdownLinkTest() {
        MarkdownService markdownService = new MarkdownService(new LanguageService());

        String markdown = "[Brightspace](https://brightspace.tudelft.nl/)";

        String html = markdownService.convertMarkdownToHtml(markdown);

        assertTrue(html.contains("<a href=\"https://brightspace.tudelft.nl/\">Brightspace</a>"));

    }
}
