package enterprises.iwakura.docs.object;

import java.util.concurrent.CompletableFuture;

import com.hypixel.hytale.server.core.asset.common.CommonAsset;

import lombok.Getter;

@Getter
public class RuntimeCommonAsset extends CommonAsset {

    private final byte[] data;

    public RuntimeCommonAsset(String name, byte[] data) {
        super(name, data);
        this.data = data;
    }

    @Override
    protected CompletableFuture<byte[]> getBlob0() {
        return CompletableFuture.completedFuture(data);
    }
}