package bbd.jportal2;

import java.util.Map;

interface ISQLProcToken {
    String getUnformattedLine();
    String getDecoratedLine(JPortalTemplateOutputOptions options);
}