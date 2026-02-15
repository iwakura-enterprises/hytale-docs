package enterprises.iwakura.docs.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ExceptionUtils {

    /**
     * Dumps exception into a string
     *
     * @param prefix Prefix to prepend
     * @param exception Exception
     *
     * @return Exception stacktrace
     */
    public static String dumpExceptionStacktrace(String prefix, Exception exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        
        String stacktrace = stringWriter.toString();
        if (prefix == null || prefix.isEmpty()) {
            return stacktrace;
        }
        
        return stacktrace.lines()
                .map(line -> prefix + line)
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
