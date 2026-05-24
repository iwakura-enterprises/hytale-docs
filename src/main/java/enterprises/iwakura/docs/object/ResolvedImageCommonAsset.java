package enterprises.iwakura.docs.object;

import enterprises.iwakura.docs.util.Vector2d;
import lombok.Data;

@Data
public class ResolvedImageCommonAsset {

    private final String commonAssetPath;
    private final Vector2d imageSize;
}
