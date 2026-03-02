package enterprises.iwakura.docs.api.hytalemodding.response;

import lombok.Data;

@Data
public abstract class HMWikiResponse {

    private String error;

    /**
     * Checks if the response errored
     *
     * @return True if yes, false otherwise
     */
    public boolean hasError() {
        return error != null;
    }

}
