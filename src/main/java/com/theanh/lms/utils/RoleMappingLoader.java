package com.theanh.lms.utils;

import com.theanh.lms.config.RoleMappingProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RoleMappingLoader {

    private final ResourceLoader resourceLoader;
    private final RoleMappingProperties properties;
    private final Map<String, Set<String>> cachedMappings = new ConcurrentHashMap<>();

    public RoleMappingLoader(ResourceLoader resourceLoader, RoleMappingProperties properties) {
        this.resourceLoader = resourceLoader;
        this.properties = properties;
        reload();
    }

    public Map<String, Set<String>> getMappings() {
        return Collections.unmodifiableMap(cachedMappings);
    }

    public Set<String> getRolesForKey(String key) {
        return cachedMappings.getOrDefault(key, Collections.emptySet());
    }

    public void reload() {
        cachedMappings.clear();
        if (!StringUtils.hasText(properties.getLocation())) {
            log.warn("Role mapping location is empty, skipping load");
            return;
        }
        Resource resource = resourceLoader.getResource(properties.getLocation());
        Properties props = new Properties();
        try (var input = resource.getInputStream()) {
            props.load(input);
            props.forEach((k, v) -> {
                String key = k.toString();
                String value = v == null ? "" : v.toString();
                Set<String> roles = Arrays.stream(value.split(","))
                        .map(String::trim)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toSet());
                cachedMappings.put(key, roles);
            });
            log.info("Loaded {} role mappings from {}", cachedMappings.size(), properties.getLocation());
        } catch (IOException e) {
            log.error("Could not load role mappings from {}", properties.getLocation(), e);
        }
    }
}
