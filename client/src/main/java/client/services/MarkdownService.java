package client.services;
import client.exceptions.WebViewException;
import jakarta.inject.Inject;
import javafx.scene.web.WebEngine;
import org.commonmark.parser.Parser;
import org.commonmark.node.Node;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.FileWriter;
import java.nio.file.Paths;

public class MarkdownService {

    private final LanguageService languageService;

    @Inject
    public MarkdownService(LanguageService languageService) {
        this.languageService = languageService;
    }

    /**
     * Initializes markdown service
     * @param webEngine the web engine to show markdown view
     */
    public void initializeMarkdown(WebEngine webEngine) {
        webEngine.getLoadWorker().exceptionProperty().addListener((_, _, newException) -> {
            if (newException != null) {
                webEngine.loadContent(showErrorHtmlView(newException));
                throw new WebViewException(newException.getMessage());
            }
        });
    }

    /**
     * Returns a html with a message from the exception
     * @param exception the exception to display
     * @return the error html
     */
    public String showErrorHtmlView(Throwable exception) {
        return "<html><body><h3 style='color:red;'>" +
                languageService.getDescriptionByKey("Item.collectionError")
                + "</h3><p style='color:red;'>"
                + exception.getMessage() + "</p></body></html>";
    }

    /**
     * Converts a markdown string to html representation
     * @param toParse the String to parse
     * @return an html String
     */
    public String convertMarkdownToHtml(String toParse){
        try {
            Parser parser = Parser.builder().build();
            Node node = parser.parse(toParse);
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            String htmlContent = renderer.render(node);

            // the content plus necessary css to style the markdown content and make it so
            // the text wraps, instead of one long line
            return "<html><head><style>"
                    + "body {"
                    + "    word-wrap: break-word;"
                    + "    white-space: pre-wrap;"
                    + "    padding-right: 12px;"
                    + "}"
                    + "pre, code {"
                    + "    word-wrap: break-word;"
                    + "    white-space: pre-wrap;"
                    + "    overflow-wrap: break-word;"
                    + "}"
                    + "</style></head><body>"
                    + htmlContent
                    + "</body></html>";
        }
        catch (Exception e) {
            System.err.println("An exception occurred with message: " + e.getMessage());
            e.printStackTrace();
            return showErrorHtmlView(e);
        }
    }

    /**
     * Creates html file from a markdown String
     * @param toParse the String to parse
     */
    public void createTemporaryHtmlFile(String toParse) {
        String html = convertMarkdownToHtml(toParse);
        String path = Paths.get(System.getProperty("java.io.tmpdir"), "markdown.html").toString();
        try (FileWriter writer = new FileWriter(path))/*could upgrade to buffered writer*/{
            writer.write(html);
        } catch (Exception e) {
            e.printStackTrace();
            throw new WebViewException(e.getMessage());
        }
    }
}