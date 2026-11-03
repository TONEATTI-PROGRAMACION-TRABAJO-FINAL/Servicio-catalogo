package com.toneatti.catalogo.synchronization.infrastructure.redis.adapter;

import com.toneatti.catalogo.config.CatedraIntegrationProperties;
import com.toneatti.catalogo.synchronization.domain.exception.CatalogIncrementUnusableException;
import com.toneatti.catalogo.synchronization.domain.exception.CatedraUnavailableException;
import com.toneatti.catalogo.synchronization.domain.model.CatalogIncrement;
import com.toneatti.catalogo.synchronization.domain.model.CatalogVersionWindow;
import com.toneatti.catalogo.synchronization.domain.ports.out.CatalogChangesRepository;
import com.toneatti.catalogo.synchronization.infrastructure.redis.dto.CatedraCatalogChangesResponse;
import com.toneatti.catalogo.synchronization.infrastructure.shared.dto.CatedraProfessionalCategoryResponse;
import com.toneatti.catalogo.synchronization.infrastructure.shared.dto.CatedraProfessionalResponse;
import com.toneatti.catalogo.synchronization.infrastructure.shared.dto.CatedraWeeklyScheduleResponse;
import com.toneatti.catalogo.synchronization.infrastructure.shared.mapper.CatedraCatalogItemMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Reads the versions, the changes and the current entities of the cátedra catalog from Redis (section 14), only
 * with read commands on the namespace the ACL allows. Messages name the key, never the credentials.
 */
@Component
public class RedisCatalogChangesRepositoryAdapter implements CatalogChangesRepository {

    private static final String CURRENT_VERSION = "current-version";
    private static final String OLDEST_AVAILABLE_VERSION = "oldest-available-version";
    private static final String CHANGES = "changes:";
    private static final String CATEGORIES = "professional-categories";
    private static final String PROFESSIONALS = "professionals";
    private static final String WEEKLY_SCHEDULES = "weekly-schedules";

    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;
    private final CatedraCatalogItemMapper catedraCatalogItemMapper;
    private final String keyPrefix;

    public RedisCatalogChangesRepositoryAdapter(StringRedisTemplate redisTemplate, JsonMapper jsonMapper,
                                                CatedraCatalogItemMapper catedraCatalogItemMapper,
                                                CatedraIntegrationProperties properties) {
        this.redisTemplate = redisTemplate;
        this.jsonMapper = jsonMapper;
        this.catedraCatalogItemMapper = catedraCatalogItemMapper;
        this.keyPrefix = properties.redis().keyPrefix();
    }

    // One MGET, so both versions come from the same moment.
    @Override
    public CatalogVersionWindow getVersionWindow() {
        String current = key(CURRENT_VERSION);
        String oldest = key(OLDEST_AVAILABLE_VERSION);
        List<String> values = read("MGET " + current + " " + oldest,
                () -> redisTemplate.opsForValue().multiGet(List.of(current, oldest)));
        if (values == null || values.size() != 2) {
            throw unusable("Redis answered MGET of the versions without both values", null);
        }
        return new CatalogVersionWindow(versionIn(current, values.get(0)), versionIn(oldest, values.get(1)));
    }

    @Override
    public CatalogIncrement getIncrement(long version) {
        String changesKey = key(CHANGES + version);
        String json = read("GET " + changesKey, () -> redisTemplate.opsForValue().get(changesKey));
        if (json == null) {
            throw unusable(changesKey + " is missing", null);
        }
        CatedraCatalogChangesResponse.Changes changes = changesIn(changesKey, json, version);
        return new CatalogIncrement(version,
                currentItems(CATEGORIES, changes.professionalCategories(), CatedraProfessionalCategoryResponse.class,
                        CatedraProfessionalCategoryResponse::id, catedraCatalogItemMapper::toDomainModel),
                currentItems(PROFESSIONALS, changes.professionals(), CatedraProfessionalResponse.class,
                        CatedraProfessionalResponse::id, catedraCatalogItemMapper::toDomainModel),
                currentItems(WEEKLY_SCHEDULES, changes.weeklySchedules(), CatedraWeeklyScheduleResponse.class,
                        CatedraWeeklyScheduleResponse::id, catedraCatalogItemMapper::toDomainModel));
    }

    private CatedraCatalogChangesResponse.Changes changesIn(String changesKey, String json, long version) {
        CatedraCatalogChangesResponse response = parse(changesKey, json, CatedraCatalogChangesResponse.class);
        if (response == null || response.version() == null || response.version() != version) {
            throw unusable(changesKey + " does not hold version " + version, null);
        }
        CatedraCatalogChangesResponse.Changes changes = response.changes();
        if (changes == null || !hasIds(changes.professionalCategories()) || !hasIds(changes.professionals())
                || !hasIds(changes.weeklySchedules())) {
            throw unusable(changesKey + " has a missing or null list of ids", null);
        }
        return changes;
    }

    private static boolean hasIds(List<Long> ids) {
        return ids != null && ids.stream().allMatch(Objects::nonNull);
    }

    // The current state of each id, read with one HMGET; an empty list does not touch the Hash.
    private <T, R> List<R> currentItems(String hash, List<Long> ids, Class<T> type, Function<T, Long> idOf,
                                        Function<T, R> toDomainModel) {
        if (ids.isEmpty()) {
            return List.of();
        }
        String hashKey = key(hash);
        List<Object> fields = ids.stream().<Object>map(String::valueOf).toList();
        List<Object> values = read("HMGET " + hashKey,
                () -> redisTemplate.opsForHash().multiGet(hashKey, fields));
        if (values == null || values.size() != ids.size()) {
            throw unusable("Redis answered HMGET " + hashKey + " without a value per id", null);
        }
        List<R> items = new ArrayList<>(ids.size());
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            Object value = values.get(i);
            if (value == null) {
                throw unusable("Id " + id + " is missing from " + hashKey, null);
            }
            T item = parse(hashKey + " " + id, value.toString(), type);
            if (item == null || !id.equals(idOf.apply(item))) {
                throw unusable("Field " + id + " of " + hashKey + " holds another entity", null);
            }
            items.add(toDomainModel.apply(item));
        }
        return items;
    }

    private long versionIn(String key, String value) {
        if (value == null) {
            throw unusable(key + " is missing", null);
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw unusable(key + " is not a decimal version", e);
        }
    }

    private <T> T parse(String source, String json, Class<T> type) {
        try {
            return jsonMapper.readValue(json, type);
        } catch (JacksonException e) {
            throw unusable(source + " is not valid JSON for " + type.getSimpleName(), e);
        }
    }

    private String key(String suffix) {
        return keyPrefix + suffix;
    }

    // Refused connections, timeouts and ACL rejections all surface as DataAccessException.
    private static <T> T read(String command, Supplier<T> redisCall) {
        try {
            return redisCall.get();
        } catch (DataAccessException e) {
            throw new CatedraUnavailableException("Redis read failed: " + command, e);
        }
    }

    private static CatalogIncrementUnusableException unusable(String message, Throwable cause) {
        return new CatalogIncrementUnusableException(message, cause);
    }
}
