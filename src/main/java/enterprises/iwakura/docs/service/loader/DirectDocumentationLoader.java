package enterprises.iwakura.docs.service.loader;

import java.util.List;

import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.LoaderContext;
import lombok.RequiredArgsConstructor;

/**
 * Provides programmatical way to load documentations by just passing
 * a list of {@link Documentation}.
 */
@RequiredArgsConstructor
public class DirectDocumentationLoader extends DocumentationLoader {

    public final List<Documentation> documentations;

    @Override
    public List<Documentation> load(LoaderContext loaderContext) {
        return documentations;
    }
}
