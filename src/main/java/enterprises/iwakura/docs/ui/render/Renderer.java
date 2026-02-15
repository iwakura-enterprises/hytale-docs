package enterprises.iwakura.docs.ui.render;

import enterprises.iwakura.docs.object.DocsContext;

public interface Renderer<T> {

    String render(DocsContext ctx, T data);

}
