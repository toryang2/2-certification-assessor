package assessor.utils;

public class MarkdownHelper {
    public static String toHtml(String markdown) {
        // Your conversion logic
        String html = markdown;
        html = html.replaceAll("(?m)^## (.*)", "<h2>$1</h2>");
        html = html.replaceAll("(?m)^- (.*)", "<li>$1</li>");
        html = html.replaceAll("(?m)(^<li>.*</li>)+", "<ul>$0</ul>");
        html = html.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");

        return """
            <html>
              <head>
                <style>
                  body {
                    font-family: 'Segoe UI', 'Segoe UI Emoji', sans-serif;
                    font-size: 13px;
                    padding: 10px;
                  }
                  h2 {
                    font-size: 16px;
                    margin-top: 10px;
                  }
                  ul {
                    padding-left: 20px;
                  }
                </style>
              </head>
              <body>
        """ + html + "</body></html>";
    }
}
