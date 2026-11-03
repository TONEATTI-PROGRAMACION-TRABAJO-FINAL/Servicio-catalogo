package com.toneatti.catalogo.synchronization.infrastructure.rest.adapter;

import com.toneatti.catalogo.synchronization.domain.exception.CatedraAuthenticationException;
import com.toneatti.catalogo.synchronization.domain.exception.CatedraUnavailableException;
import com.toneatti.catalogo.synchronization.domain.model.CatalogSnapshot;
import com.toneatti.catalogo.synchronization.domain.ports.out.CatedraRepository;
import com.toneatti.catalogo.synchronization.infrastructure.rest.dto.CatedraProblemResponse;
import com.toneatti.catalogo.synchronization.infrastructure.rest.dto.CatedraSnapshotResponse;
import com.toneatti.catalogo.synchronization.infrastructure.rest.mapper.CatedraSnapshotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Calls GET /api/synchronization/snapshot of the cátedra (section 7 of the integration contract) with the
 * {@link RestClient} defined in {@link com.toneatti.catalogo.synchronization.infrastructure.rest.config.CatedraRestClientConfig}.
 * It decides by status and code, never by detail (section 13). Messages name only the request, never the JWT.
 */
@Component
@RequiredArgsConstructor
public class RestCatedraRepositoryAdapter implements CatedraRepository {

    private static final String SNAPSHOT_PATH = "/api/synchronization/snapshot";
    private static final String REQUEST = "GET " + SNAPSHOT_PATH;

    private final RestClient catedraRestClient;
    private final CatedraSnapshotMapper catedraSnapshotMapper;

    @Override
    public CatalogSnapshot getSnapshot() {
        CatedraSnapshotResponse body = fetch();
        // Empty collections are a valid snapshot; a missing version or collection is not.
        if (body == null || body.snapshotVersion() == null || body.professionalCategories() == null
                || body.professionals() == null || body.weeklySchedules() == null) {
            throw new CatedraUnavailableException("Catedra answered " + REQUEST + " without a complete snapshot", null);
        }
        return catedraSnapshotMapper.toDomainModel(body);
    }

    // Error statuses, timeouts, refused connections and unreadable bodies all surface as RestClientException.
    private CatedraSnapshotResponse fetch() {
        try {
            return catedraRestClient.get()
                    .uri(SNAPSHOT_PATH)
                    .retrieve()
                    .body(CatedraSnapshotResponse.class);
        } catch (HttpClientErrorException e) {
            String answer = describe(e);
            if (e.getStatusCode().isSameCodeAs(HttpStatus.UNAUTHORIZED)
                    || e.getStatusCode().isSameCodeAs(HttpStatus.FORBIDDEN)) {
                // With or without a code, the technical JWT is not accepted: retrying cannot help.
                throw new CatedraAuthenticationException("Catedra rejected the technical JWT on " + REQUEST
                        + " with " + answer, e);
            }
            throw new CatedraUnavailableException("Catedra request failed: " + REQUEST + " with " + answer, e);
        } catch (RestClientException e) {
            throw new CatedraUnavailableException("Catedra request failed: " + REQUEST, e);
        }
    }

    private static String describe(HttpClientErrorException e) {
        String status = String.valueOf(e.getStatusCode().value());
        try {
            CatedraProblemResponse problem = e.getResponseBodyAs(CatedraProblemResponse.class);
            return problem == null || problem.code() == null || problem.code().isBlank()
                    ? status : status + " " + problem.code();
        } catch (RuntimeException unreadable) {
            return status;
        }
    }
}
