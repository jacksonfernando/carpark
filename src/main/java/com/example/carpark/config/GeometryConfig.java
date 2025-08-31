package com.example.carpark.config;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeometryConfig {

    @Bean
    public GeometryFactory geometryFactory() {
        // Use SRID 4326 (WGS84) with double precision
        return new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    }
}
