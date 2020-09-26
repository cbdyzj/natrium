package nano.web.scripting;

import org.graalvm.polyglot.Context;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;

public class Scripting {

    /**
     * @see <a href="https://html.spec.whatwg.org/multipage/scripting.html#scriptingLanguages">Scripting languages</a>
     */
    public static final MediaType TEXT_JAVASCRIPT = MediaType.parseMediaType("text/javascript");

    public static String eval(@NotNull String script) {
        var value = Context.create("js").eval("js", script);
        return String.valueOf(value);
    }
}
