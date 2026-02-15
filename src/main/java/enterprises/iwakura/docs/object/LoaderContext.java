package enterprises.iwakura.docs.object;

import com.google.gson.Gson;

import enterprises.iwakura.docs.service.MarkdownService;
import enterprises.iwakura.docs.util.Logger;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class LoaderContext {

    private final Logger logger;
    private final Gson gson;
    private final MarkdownService markdownService;

}
