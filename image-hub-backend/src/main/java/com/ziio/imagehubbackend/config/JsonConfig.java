package com.ziio.imagehubbackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * spring mvc json 配置
 */
@JsonComponent
public class JsonConfig {

    @Bean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder){
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        SimpleModule modeule = new SimpleModule();
        modeule.addSerializer(Long.class, ToStringSerializer.instance);
        modeule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(modeule);
        return objectMapper;
    }
}
