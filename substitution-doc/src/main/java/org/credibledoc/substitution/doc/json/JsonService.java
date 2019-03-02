package org.credibledoc.substitution.doc.json;

import com.credibledoc.substitution.exception.SubstitutionRuntimeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Contains dedicated {@link #objectMapper} and provides a proxy for its services.
 * <p>
 * Provides serialization and deserialization methods.
 *
 * @author Kyrylo Semenko
 */
@Service
public class JsonService {

    /**
     * Dedicated {@link ObjectMapper}
     */
    private ObjectMapper objectMapper;

    @PostConstruct
    public void postConstruct() {
        objectMapper = new ObjectMapper();
    }

    public <T> T readValue(String json, Class<T> valueClass) {
        try {
            return objectMapper.readValue(json, valueClass);
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
    }
}
