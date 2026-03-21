package enterprises.iwakura.docs.integration.hytalemodding.api.response;

import lombok.Data;

@Data
public class HMWikiResponse {

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
