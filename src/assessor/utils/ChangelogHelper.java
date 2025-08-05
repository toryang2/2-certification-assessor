package assessor.utils;

import javax.swing.*;
import java.awt.*;

public class ChangelogHelper {

    public static JScrollPane createChangelogPane(String markdown) {
        JEditorPane textPane = new JEditorPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setOpaque(false);
        textPane.putClientProperty("JEditorPane.honorDisplayProperties", Boolean.TRUE);

        // Use custom font stack: Segoe UI Emoji fallback
        String htmlContent = """
            <html>
          <head>
            <style>
              body {
                font-family: "Segoe UI Emoji";
                font-size: 11px;
                line-height: 1.4;
              }
              h1, h2, h3 {
                margin-top: 1em;
                margin-bottom: 0.5em;
              }
              ul {
                margin: 0.5em 0;
                padding-left: 1.2em;
              }
            </style>
          </head>
          <body>
                %s
              </body>
            </html>
            """.formatted(markdownToHtml(markdown));

        textPane.setText(htmlContent);

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        scrollPane.setBorder(null);
        return scrollPane;
    }

    private static String markdownToHtml(String markdown) {
        if (markdown == null) return "";

        String html = markdown;

        // Convert Markdown to basic HTML
        html = html.replaceAll("(?m)^## (.*?)$", "<h3>$1</h3>");
        html = html.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
        html = html.replaceAll("(?m)^- (.*?)$", "<li>$1</li>");
        html = html.replaceAll("(?s)(<li>.*?</li>)", "<ul>$1</ul>");

        // Replace newlines
        html = html.replace("\n", "<br>");

        return html;
    }

}
