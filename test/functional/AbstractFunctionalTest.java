package functional;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import constants.Config;
import constants.Constants;
import enums.APIField;
import interfaces.Field;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

/**
 * General Functional Test
 */
public abstract class AbstractFunctionalTest extends FunctionalTest {

    /**
     * Create a Query String from HashMap Parameters
     *
     * @param keyValueMap HashMap
     * @return query string
     */
    public static String createQueryString(HashMap<String, String> keyValueMap) {
        StringBuilder qs = new StringBuilder();
        keyValueMap.forEach((k, v) -> {
                    if (qs.length() > 0) {
                        qs.append('&');
                    }
                    try {
                        qs.append(URLEncoder.encode(k, String.valueOf(StandardCharsets.UTF_8))).append('=').append(URLEncoder.encode(v, String.valueOf(StandardCharsets.UTF_8)));
                    } catch (UnsupportedEncodingException e) {
                        Logger.error(e.getMessage());
                    }
                }
        );
        return "?" + qs.toString();
    }

    /**
     * Create URI
     *
     * @param url Action path and parameters
     * @return PLAY Http.path + url
     */
    public String createURI(String url) {
        return Constants.DELIMITER_PATH + Config.VERSION + url.trim();
    }

    /**
     * Asserts a <em>2Ox Success</em> response
     *
     * @param response server response
     */
    public static void assertIsSuccess(Http.Response response) {
        assertThat(getResponseMessage(response), response.status, anyOf(is(200), is(201), is(204)));
    }

    /**
     * Asserts a <em>2O1 created</em> response
     *
     * @param response server response
     */
    public static void assertIsCreated(Http.Response response) {
        assertEquals(getResponseMessage(response), 201, response.status.intValue());
    }

    /**
     * Asserts a <em>204 no content</em> response
     *
     * @param response server response
     */
    public static void assertIsNoContent(Http.Response response) {
        assertEquals(getResponseMessage(response), 204, response.status.intValue());
    }

    /**
     * Asserts a <em>401 or 403 Unauthorized</em> response
     *
     * @param response server response
     */
    public static void assertIsUnauthorized(Http.Response response) {
        assertThat(getResponseMessage(response), response.status, anyOf(is(401), is(403)));
    }

    /**
     * Asserts a <em>412</em> response
     *
     * @param response server response
     */
    public static void assertIsPreconditionFailure(Http.Response response) {
        assertThat(getResponseMessage(response), response.status, anyOf(is(412)));
    }

    /**
     * Asserts a <em>404 Not found</em> response
     *
     * @param response server response
     */
    public static void assertIsNotFound(Http.Response response) {
        assertEquals(getResponseMessage(response), 404, response.status.intValue());
    }

    /**
     * Asserts a <em>409</em> response
     *
     * @param response server response
     */
    public static void assertIsConflict(Http.Response response) {
        assertThat(getResponseMessage(response), response.status, anyOf(is(409)));
    }

    /**
     * Asserts a <em>400</em> response
     *
     * @param response server response
     */
    public static void assertIsBadRequest(Http.Response response) {
        assertThat(getResponseMessage(response), response.status, is(400));
    }

    /**
     * Asserts a <em>3Ox Success</em> response
     *
     * @param response server response
     */
    public static void assertIsStatusCd(Http.Response response, int statusCd) {
        assertThat(getResponseMessage(response), response.status, anyOf(is(statusCd)));
    }

    /**
     * Check if jsonObj matches with objectClass. (Deserialization)
     *
     * @param jsonObj     json Object
     * @param objectClass Target Object Class
     */
    public Object getObjectFromJson(JsonObject jsonObj, Class objectClass) {
        Object obj = new Gson().fromJson(jsonObj.getAsString(), objectClass);
        assertNotNull(obj);
        return obj;
    }

    /**
     * Check if json response includes names (keys).
     *
     * @param jsonObj Json Object
     * @param names   names
     */
    public void doesInclude(JsonObject jsonObj, String... names) {
        Arrays.asList(names).forEach(name -> assertNotNull(jsonObj.get(name)));
    }

    /**
     * Check if format of array elements are matched with related modes.
     *
     * @param jsonObj     Json Object
     * @param jsonArrName Object array name in json Object.
     * @param objectClass Model class
     */
    public void isArrayFormatMatched(JsonObject jsonObj, String jsonArrName, Class objectClass) {
        JsonArray jsonArray = jsonObj.getAsJsonArray(jsonArrName);
        assertNotNull(jsonArray);
        for (int i = 0; i < jsonArray.size(); i++) {
            getObjectFromJson(jsonArray.get(i).getAsJsonObject(), objectClass);
        }
    }

    /**
     * Convert Response to JsonObject
     * <br/>
     * Note: add "%test.gzip=disabled" to PLAY! configuration since Gzip decompression does not work.
     *
     * @param response Http.Response
     * @return JSONObject of response.
     */
    public static JsonObject getJsonFromResponse(final Http.Response response) {
        StringBuilder decompressed = new StringBuilder();
        JsonParser parser = new JsonParser();
        if (null != response.getHeader("Content-Encoding") && response.getHeader("Content-Encoding").equals("gzip")) {
            try {
                GZIPInputStream gzipStream = new GZIPInputStream(new ByteArrayInputStream(getContent(response).getBytes("utf-8")));
                BufferedReader bf = new BufferedReader(new InputStreamReader(gzipStream));
                String line;
                while ((line = bf.readLine()) != null) {
                    decompressed.append(line);
                }
            } catch (IOException e) {
                Logger.error(e.getMessage());
                assertNotNull(null);
            }
        } else {
            return parser.parse(getContent(response)).getAsJsonObject();
        }
        return parser.parse(decompressed.toString()).getAsJsonObject();
    }

    public static Optional<String> getIdFromResponse(final Http.Response response) {
        try {
            JsonObject obj = getJsonFromResponse(response);
            if (obj != null && obj.get(APIField.ID.field()) != null) {
                return Optional.ofNullable(obj.get(APIField.ID.field()).getAsString());
            }
        } catch (Exception e) {
        }
        return Optional.empty();
    }

    public static String getResponseMessage(final Http.Response response) {
        try {
            return getJsonFromResponse(response).get(APIField.MESSAGE.field()).getAsString();
        } catch (Exception e) {
            return "";
        }
    }

    public static class ParamBuilder {
        Http.Request request = newRequest();

        public ParamBuilder param(final Field key, final Object value) {
            request.params.put(key.field(), String.valueOf(value));
            return this;
        }

        public <T, U> ParamBuilder param(final String key, final Map<T, U> values) {
            for (T k : values.keySet()) {
                request.params.put(key + "[" + k + "]", String.valueOf(values.get(k)));
            }
            return this;
        }

        public ParamBuilder param(final String key, final Collection<String> values) {
            for (String value : values) {
                request.params.put(key, value);
            }
            return this;
        }

        public ParamBuilder header(final String key, final String name, final String value) {
            request.headers.put(key, new Http.Header(name, value));
            return this;
        }

        public Http.Request build() {
            return request;
        }
    }

    public static Http.Response PUT(final Http.Request request, final Object url) {
        return PUT(request, url, APPLICATION_X_WWW_FORM_URLENCODED, StringUtils.EMPTY);
    }

    public static Http.Response DELETE(final Http.Request request, final Object url, final String contenttype, final String body) {
        String path;
        String queryString = "";
        String turl = url.toString();
        if (turl.contains("?")) {
            path = turl.substring(0, turl.indexOf("?"));
            queryString = turl.substring(turl.indexOf("?") + 1);
        } else {
            path = turl;
        }
        request.method = "DELETE";
        request.contentType = contenttype;
        request.url = turl;
        request.path = path;
        request.querystring = queryString;
        request.body = new ByteArrayInputStream(body.getBytes());
        return makeRequest(request);
    }
}
