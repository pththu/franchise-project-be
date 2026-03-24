package com.franchiseproject.orderservice.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class GeoapifyMatrixResponse {
    private List<Source> sources;
    private List<Target> targets;
    private List<List<Result>> sources_to_targets;

    @Data
    public static class Result {
        private Double distance; // mét
        private Double time;     // giây
    }

    @Data
    public static class Source {
        private Double lat;
        private Double lon;
    }

    @Data
    public static class Target {
        private Double lat;
        private Double lon;
    }

}
