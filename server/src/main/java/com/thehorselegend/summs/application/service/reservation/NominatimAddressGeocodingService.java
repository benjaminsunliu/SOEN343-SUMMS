package com.thehorselegend.summs.application.service.reservation;

import com.thehorselegend.summs.api.dto.AddressSuggestionDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehorselegend.summs.domain.vehicle.Location;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class NominatimAddressGeocodingService implements AddressGeocodingService {

    private static final String GEOCODING_UNAVAILABLE_MESSAGE = "Geocoding service is unavailable";
    private static final String USER_AGENT = "SUMMS/1.0 (reservations@summs.local)";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(8);
    private static final String NOMINATIM_ENDPOINT = "https://nominatim.openstreetmap.org/search";
    private static final String NOMINATIM_REVERSE_ENDPOINT = "https://nominatim.openstreetmap.org/reverse";
    private static final int MAX_LIMIT = 10;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ConcurrentMap<String, Location> geocodeCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AddressSuggestionDto> reverseCache = new ConcurrentHashMap<>();

    public NominatimAddressGeocodingService(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public Location geocode(String address, String city) {
        String normalizedAddress = normalize(address);
        String normalizedCity = normalize(city);
        if (normalizedAddress.isBlank()) {
            throw new IllegalArgumentException("Address is required");
        }
        String cacheKey = normalizedAddress + "|" + normalizedCity;

        return geocodeCache.computeIfAbsent(cacheKey, ignored -> {
            for (String query : buildGeocodeQueries(normalizedAddress, normalizedCity)) {
                List<AddressSuggestionDto> suggestions = suggestAddresses(query, "", 5);
                AddressSuggestionDto bestMatch = findBestMatch(suggestions, normalizedCity);
                if (bestMatch != null) {
                    return new Location(bestMatch.latitude(), bestMatch.longitude());
                }
            }

            throw new IllegalArgumentException("Address could not be geocoded: " + normalizedAddress);
        });
    }

    @Override
    public List<AddressSuggestionDto> suggestAddresses(String query, String city, int limit) {
        String normalizedQuery = normalize(query);
        String normalizedCity = normalize(city);
        if (normalizedQuery.isBlank()) {
            return List.of();
        }

        int normalizedLimit = normalizeLimit(limit);
        URI uri = buildSearchUri(buildQuery(normalizedQuery, normalizedCity), normalizedLimit);
        JsonNode root = fetchJson(uri);

        if (!root.isArray() || root.isEmpty()) {
            return List.of();
        }

        return streamArray(root).map(node -> {
                    String address = normalize(node.path("display_name").asText());
                    Double latitude = parseCoordinate(node.path("lat").asText());
                    Double longitude = parseCoordinate(node.path("lon").asText());
                    String suggestionCity = extractCity(node.path("address"), address);
                    return new AddressSuggestionDto(address, suggestionCity, latitude, longitude);
                })
                .filter(suggestion -> !suggestion.address().isBlank())
                .toList();
    }

    @Override
    public List<String> suggestCities(String query, int limit) {
        String normalizedQuery = normalize(query);
        if (normalizedQuery.isBlank()) {
            return List.of();
        }

        int normalizedLimit = normalizeLimit(limit);
        URI uri = buildCitySearchUri(normalizedQuery, normalizedLimit);
        JsonNode root = fetchJson(uri);

        if (!root.isArray() || root.isEmpty()) {
            return List.of();
        }

        Set<String> cities = new LinkedHashSet<>();
        streamArray(root).forEach(node -> {
            String city = extractCity(node.path("address"), node.path("display_name").asText());
            if (!city.isBlank()) {
                cities.add(city);
            }
        });

        return cities.stream().limit(normalizedLimit).toList();
    }

    @Override
    public AddressSuggestionDto reverseGeocode(double latitude, double longitude) {
        String cacheKey = String.format(Locale.US, "%.5f,%.5f", latitude, longitude);
        AddressSuggestionDto cached = reverseCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        URI uri = buildReverseUri(latitude, longitude);
        JsonNode root = fetchJson(uri);

        String address = normalize(root.path("display_name").asText());
        if (address.isBlank()) {
            throw new IllegalArgumentException("Address could not be reverse geocoded");
        }

        String city = extractCity(root.path("address"), address);
        AddressSuggestionDto result = new AddressSuggestionDto(address, city, latitude, longitude);
        reverseCache.putIfAbsent(cacheKey, result);
        return result;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private String buildQuery(String address, String city) {
        if (address.isBlank()) {
            throw new IllegalArgumentException("Address is required");
        }

        if (city.isBlank()) {
            return address;
        }

        if (address.toLowerCase().contains(city.toLowerCase())) {
            return address;
        }

        return address + ", " + city;
    }

    private List<String> buildGeocodeQueries(String address, String city) {
        Set<String> queries = new LinkedHashSet<>();
        queries.add(buildQuery(address, city));
        queries.add(address);

        String sanitizedAddress = sanitizeAddress(address);
        if (!sanitizedAddress.isBlank()) {
            queries.add(sanitizedAddress);
            if (!city.isBlank()) {
                queries.add(buildQuery(sanitizedAddress, city));
            }
        }

        List<String> parts = splitAddressParts(sanitizedAddress.isBlank() ? address : sanitizedAddress);
        if (parts.size() > 1) {
            String withoutLeadingLabel = String.join(", ", parts.subList(1, parts.size()));
            queries.add(withoutLeadingLabel);
            if (!city.isBlank()) {
                queries.add(buildQuery(withoutLeadingLabel, city));
            }
        }

        String streetLevelQuery = buildStreetLevelQuery(parts, city);
        if (!streetLevelQuery.isBlank()) {
            queries.add(streetLevelQuery);
        }

        return queries.stream()
                .map(this::normalize)
                .filter(query -> !query.isBlank())
                .toList();
    }

    private AddressSuggestionDto findBestMatch(List<AddressSuggestionDto> suggestions, String city) {
        List<AddressSuggestionDto> validSuggestions = suggestions.stream()
                .filter(suggestion -> suggestion.latitude() != null && suggestion.longitude() != null)
                .toList();
        if (validSuggestions.isEmpty()) {
            return null;
        }

        String normalizedCity = normalize(city).toLowerCase(Locale.ROOT);
        if (!normalizedCity.isBlank()) {
            for (AddressSuggestionDto suggestion : validSuggestions) {
                String suggestionCity = normalize(suggestion.city()).toLowerCase(Locale.ROOT);
                if (!suggestionCity.isBlank()
                        && (suggestionCity.contains(normalizedCity)
                        || normalizedCity.contains(suggestionCity))) {
                    return suggestion;
                }
            }
        }

        return validSuggestions.get(0);
    }

    private String sanitizeAddress(String address) {
        String normalized = normalize(address);
        if (normalized.isBlank()) {
            return normalized;
        }

        String withoutCountry = normalized.replaceAll("(?i),\\s*canada\\s*$", "");
        String withoutPostalCode = withoutCountry.replaceAll(
                "(?i)\\b[ABCEGHJ-NPRSTVXY]\\d[ABCEGHJ-NPRSTV-Z][ -]?\\d[ABCEGHJ-NPRSTV-Z]\\d\\b",
                ""
        );
        return withoutPostalCode
                .replaceAll("\\s+,", ",")
                .replaceAll(",\\s*,", ", ")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }

    private List<String> splitAddressParts(String address) {
        return java.util.Arrays.stream(address.split(","))
                .map(this::normalize)
                .filter(part -> !part.isBlank())
                .toList();
    }

    private String buildStreetLevelQuery(List<String> parts, String city) {
        if (parts.isEmpty()) {
            return "";
        }

        int startIndex = 0;
        if (parts.size() > 1 && !containsDigit(parts.get(0)) && containsDigit(parts.get(1))) {
            startIndex = 1;
        }

        String streetLine;
        if (parts.size() > startIndex + 1 && containsDigit(parts.get(startIndex))) {
            streetLine = parts.get(startIndex) + " " + parts.get(startIndex + 1);
        } else {
            streetLine = parts.get(startIndex);
        }

        if (streetLine.isBlank()) {
            return "";
        }
        if (city.isBlank()) {
            return streetLine;
        }
        return buildQuery(streetLine, city);
    }

    private boolean containsDigit(String value) {
        for (int i = 0; i < value.length(); i += 1) {
            if (Character.isDigit(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private URI buildSearchUri(String query, int limit) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String uri = NOMINATIM_ENDPOINT
                + "?format=jsonv2&limit=" + limit + "&addressdetails=1&q=" + encodedQuery;
        return URI.create(uri);
    }

    private URI buildCitySearchUri(String query, int limit) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String uri = NOMINATIM_ENDPOINT
                + "?format=jsonv2&limit=" + limit + "&addressdetails=1&featuretype=city&q=" + encodedQuery;
        return URI.create(uri);
    }

    private URI buildReverseUri(double latitude, double longitude) {
        String uri = NOMINATIM_REVERSE_ENDPOINT
                + "?format=jsonv2&addressdetails=1&lat=" + latitude + "&lon=" + longitude;
        return URI.create(uri);
    }

    private JsonNode fetchJson(URI uri) {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("Accept", "application/json")
                .header("User-Agent", USER_AGENT)
                .GET()
                .timeout(REQUEST_TIMEOUT)
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException exception) {
            throw new IllegalStateException(GEOCODING_UNAVAILABLE_MESSAGE, exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(GEOCODING_UNAVAILABLE_MESSAGE, exception);
        }

        if (response.statusCode() != 200) {
            throw new IllegalStateException(GEOCODING_UNAVAILABLE_MESSAGE);
        }

        try {
            return objectMapper.readTree(response.body());
        } catch (IOException exception) {
            throw new IllegalStateException(GEOCODING_UNAVAILABLE_MESSAGE, exception);
        }
    }

    private Double parseCoordinate(String value) {
        String normalized = normalize(value);
        if (normalized.isBlank()) {
            return null;
        }

        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String extractCity(JsonNode addressNode, String displayName) {
        String[] cityFields = new String[]{"city", "town", "village", "municipality", "hamlet", "state"};
        if (addressNode != null && addressNode.isObject()) {
            for (String field : cityFields) {
                String value = normalize(addressNode.path(field).asText());
                if (!value.isBlank()) {
                    return value;
                }
            }
        }

        String normalizedDisplay = normalize(displayName);
        if (normalizedDisplay.isBlank()) {
            return "";
        }

        String[] parts = normalizedDisplay.split(",");
        return parts.length > 1 ? parts[1].trim() : parts[0].trim();
    }

    private int normalizeLimit(int limit) {
        if (limit < 1) {
            return 1;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private java.util.stream.Stream<JsonNode> streamArray(JsonNode arrayNode) {
        Iterable<JsonNode> iterable = arrayNode::elements;
        return java.util.stream.StreamSupport.stream(iterable.spliterator(), false);
    }
}
