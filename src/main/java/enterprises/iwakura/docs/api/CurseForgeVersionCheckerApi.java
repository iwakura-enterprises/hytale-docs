package enterprises.iwakura.docs.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;

import enterprises.iwakura.kirara.core.ApiRequest;
import enterprises.iwakura.kirara.core.Kirara;
import enterprises.iwakura.kirara.core.PathParameter;
import enterprises.iwakura.kirara.gson.GsonSerializer;
import enterprises.iwakura.kirara.httpclient.HttpClientHttpCore;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.Data;

@Bean
public class CurseForgeVersionCheckerApi extends Kirara {

    public static final Long PROJECT_ID = 1454078L;
    public static final Pattern VERSION_PATTERN = Pattern.compile(".*-(\\d+\\.\\d+\\.\\d+)\\.jar$");

    public CurseForgeVersionCheckerApi(Gson gson) {
        super(new HttpClientHttpCore(), new GsonSerializer(gson));
        setApiUrl("https://www.curseforge.com/api/v1");
    }

    /**
     * Fetches versions from CurseForge
     *
     * @return ApiRequest of Response
     */
    public ApiRequest<Response> fetch() {
        return this.createRequest("GET",
                "/mods/{project-id}/files?pageIndex=0&pageSize=20&sort=dateCreated&sortDescending=true&removeAlphas=true",
                Response.class)
            .withPathParameter(PathParameter.of("project-id", PROJECT_ID.toString()));
    }

    @Data
    public static class Response {

        private final List<FileData> data = new ArrayList<>();
        private Pagination pagination;

        /**
         * Returns the version number from the latest file data
         *
         * @return Optional version number (e.g. 1.3.0)
         */
        public Optional<String> getLatestVersionNumber() {
            return data.stream()
                .sorted(Comparator.comparing(FileData::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(FileData::getFileName)
                .map(VERSION_PATTERN::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group(1))
                .findFirst();
        }

        @Data
        public static class FileData {

            private final List<String> gameVersions = new ArrayList<>();
            private final List<Long> gameVersionTypeIds = new ArrayList<>();
            private long id;
            private String dateCreated;
            private String dateModified;
            private String displayName;
            private long fileLength;
            private String fileName;
            private int status;
            private long projectId;
            private int releaseType;
            private long totalDownloads;
            private User user;
            private int additionalFilesCount;
            private boolean hasServerPack;
            private int additionalServerPackFilesCount;
            private boolean isEarlyAccessContent;
            private boolean isCompatibleWithClient;

        }

        @Data
        public static class User {

            private long id;
            private String username;
            private String twitchAvatarUrl;
            private String displayName;

        }

        @Data
        public static class Pagination {

            private int index;
            private int pageSize;
            private int totalCount;

        }
    }
}
