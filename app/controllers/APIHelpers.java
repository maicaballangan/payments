package controllers;

import com.google.common.collect.Maps;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.stripe.exception.StripeException;
import constants.Config;
import constants.Constants;
import enums.APIErrorCode;
import enums.APIField;
import enums.MimeType;
import exceptions.HttpException;
import extensions.Version;
import org.apache.commons.lang.StringUtils;
import org.jboss.netty.handler.codec.http.HttpMethod;
import play.Logger;
import play.Play;
import play.data.validation.Validation;
import play.libs.Codec;
import play.libs.WS;
import play.mvc.*;
import play.utils.Utils;
import utils.DateUtils;
import utils.SerializationUtils;
import utils.StringExtensions;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

/**
 * APIHelpers
 * <p>
 * A Singleton cross-cutting utility class for the {@link controllers} package. All pre-flighting, rate limiting,
 * http logging, and authentication/authorization functionality is defined here.
 * </p>
 *
 * @author Maica Ballangan
 * @since v1
 */
public class APIHelpers extends Controller {

    public static final String GZIP_ENABLED = Play.configuration.getProperty("gzip");
    public static final String LAST_MODIFIED = "X-Last-Modified";
    public static final String CACHE_PERIOD = "X-Cache-Period";
    public static final String X_ETAG = "X-Etag";
    public static final String API_KEY = "api-key";
    public static final String ALLOWED_HEADERS = API_KEY + "," + Config.MAC_ADDRESS + ",Content-Type,Vary";
    public static final String ALLOWED_CREDENTIALS = "true";
    public static final String EXPOSED_HEADERS = "set-cookie";
    public static final String ALLOWED_METHODS = "PUT,GET,POST,DELETE,OPTIONS";
    public static final String MAX_AGE_LIMIT = "3628800";
    public static final String ERROR_CODES = "error-codes";
    public static final String EXTERNAL_IP_PROVIDER = Play.configuration.getProperty("externalip.host");
    public static final String EXTERNAL_IP_HEADER = Play.configuration.getProperty("externalip.header");
    public static final String DEFAULT_EXTERNAL_IP = Play.configuration.getProperty("externalip.default");
    private static final String DNS_IP = Play.configuration.getProperty("externaldns.ip");
    private static final int DNS_IP_PORT = Integer.parseInt(Play.configuration.getProperty("externaldns.port"));
    private static final String JSON_EXPRESSION = "{\"%s\":\"%s\"}";
    public static final String GET_ALL_TEMPLATE = "Application/getAll.json";

    enum BodyHttpMethod {
        POST,
        PUT,
        DELETE
    }

    /**
     * Cross Origin Request Sharing headers for pre-flight calls
     */
    @Util
    static void optionsHeaders() {
        if (request.headers.containsKey(HttpHeaders.ORIGIN.toLowerCase())) {
            final String origin = request.headers.get(HttpHeaders.ORIGIN.toLowerCase()).value();
            Logger.trace("ORIGIN: %s", origin);
            response.headers.put(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, new Http.Header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin));
            response.headers.put(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, new Http.Header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, ALLOWED_HEADERS));
            response.headers.put(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, new Http.Header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS));
            response.headers.put(HttpHeaders.ACCESS_CONTROL_MAX_AGE, new Http.Header(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE_LIMIT));
            response.headers.put(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, new Http.Header(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, ALLOWED_CREDENTIALS));
            response.headers.put(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, new Http.Header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, EXPOSED_HEADERS));
        }
    }

    @Util
    static void cacheHeaders(final String duration) {
        try {
            request.args.put(LAST_MODIFIED, request.headers.containsKey(HttpHeaders.IF_MODIFIED_SINCE) ? Utils.getHttpDateFormatter().parse(request.headers.get(HttpHeaders.IF_MODIFIED_SINCE).value()).getTime() : DateUtils.getCurrentEpochTime());
        } catch (ParseException e) {
            Logger.warn("Unable to parse %s: %s", HttpHeaders.IF_MODIFIED_SINCE, request.headers.get(HttpHeaders.IF_MODIFIED_SINCE).value());
        }
        request.args.put(CACHE_PERIOD, duration);
    }

    @Util
    static void cacheHeaders(final String duration, final String ETag) {
        cacheHeaders(duration);
        request.args.put(X_ETAG, ETag);
    }

    /**
     * Cross Origin Request Sharing calls are going to have a pre-flight option call
     */
    public static void options(final String path) {
        Logger.trace("OPTIONS path:%s", path);
        optionsHeaders();
    }

    /**
     * Authentication for http requests
     */
    @Util
    static void authenticate() throws HttpException {
        Logger.trace("Authenticating request...");
        optionsHeaders();

        // TODO: PKI
        /*if (request.headers.get(HttpHeaders.REFERER) == null || StringUtils.isBlank(request.headers.get(HttpHeaders.REFERER).value())) {
            throw new HttpException(APIErrorCode.REFERER_MISSING);
        }

        if (Config.REFERERS.stream().noneMatch(r -> r.equals(request.headers.get(HttpHeaders.REFERER).value()))) {
            throw new HttpException(APIErrorCode.AUTHORIZATION_FAILED);
        }*/
    }

    @Catch(value = HttpException.class, priority = 1)
    public static void renderHttpException(final HttpException e) {
        request.format = MimeType.json.name();
        response.status = e.getHttpStatusCode();
        Logger.error(e, e.getMessage());
        render(Application.ERROR_PAGE, e);
    }

    @Catch(value = Throwable.class, priority = 2)
    public static void renderException(final Throwable ex) {
        HttpException e;
        if (ex instanceof StripeException) {
            StripeException se = (StripeException) ex;
            e = new HttpException(se.getStatusCode(), APIErrorCode.STRIPE_EXCEPTION, se.getMessage());
        } else if (ex instanceof IllegalStateException) {
            e = new HttpException(APIErrorCode.ILLEGAL_STATE_ERROR, ex);
        } else if (ex instanceof IllegalArgumentException) {
            e = new HttpException(APIErrorCode.ILLEGAL_ARGUMENT_EXCEPTION, ex);
        } else {
            e = new HttpException(APIErrorCode.INTERNAL_SERVER_ERROR, ex);
        }
        renderHttpException(e);
    }

    @Before(priority = 3)
    protected static void checkBody() {
        try {
            if (!HttpMethod.GET.getName().equals(request.method) && !BodyHttpMethod.valueOf(request.method).name().isEmpty() && StringUtils.isNotEmpty(request.querystring)) {
                renderHttpException(new HttpException(APIErrorCode.USE_POST_BODY));
            }
        } catch (IllegalArgumentException e) {
            Logger.trace("%s using %s and content-type = %s", request.method, request.querystring, request.contentType);
        }
    }

    @Before(priority = 4)
    protected static void annotationCheck() throws HttpException {
        Method actionMethod = request.invokedMethod;
        if (actionMethod.isAnnotationPresent(Version.class)) {
            Version version = actionMethod.getAnnotation(Version.class);
            String[] appVersion = StringUtils.split(Config.API_VERSION, Constants.DELIMITER_DOT);
            String[] reqVersion = StringUtils.split(version.value(), Constants.DELIMITER_DOT);
            String[] environments = version.env();
            if (Arrays.stream(environments).anyMatch(Play.id::startsWith)) {
                for (int i = 0; i < reqVersion.length; i++) {
                    if (Integer.parseInt(reqVersion[i]) > Integer.parseInt(appVersion[i])) {
                        throw new HttpException(APIErrorCode.VERSION_INVALID, "This feature is not available in version " + Config.API_VERSION + " but will be released in version " + version.value());
                    }
                }
            }
        }

        if (actionMethod.getParameterCount() > 0) {
            validate();
        }
    }

    @Finally
    protected static void logRequest(final Throwable ex) throws UnknownHostException {
        Logger.trace("Logging request...");
        String session = request.headers.containsKey(API_KEY) ? request.headers.get(API_KEY).value() : null;

        int originalSize = response.out.size();
        int gzipSize = 0;
        Float sizeReduction = 0f;
        if (!HttpMethod.GET.getName().equals(request.method) && StringUtils.isNotBlank(request.format)) {
            String lastModified = request.args.containsKey(LAST_MODIFIED) ? request.args.get(LAST_MODIFIED).toString() : null;
            String cachePeriod = request.args.containsKey(CACHE_PERIOD) ? request.args.get(CACHE_PERIOD).toString() : null;
            String text = response.out.toString();
            if (StringUtils.isNotBlank(lastModified) && StringUtils.isNotBlank(cachePeriod)) {
                String ETag = request.args.containsKey(X_ETAG) ? request.args.get(X_ETAG).toString() : String.valueOf(text.hashCode());
                response.setHeader(HttpHeaders.VARY, API_KEY + ", User-Agent");
                if (!request.isModified(ETag, Long.valueOf(lastModified))) {
                    response.status = Http.StatusCode.NOT_MODIFIED;
                    response.setHeader(HttpHeaders.ETAG, ETag);
                }
                response.cacheFor(ETag, cachePeriod, Long.valueOf(lastModified));
            } else {
                response.setHeader(HttpHeaders.CACHE_CONTROL, "private, no-cache, no-store, must-revalidate, max-age=0");
            }
            if ((MimeType.json.name().equals(request.format) || MimeType.html.name().equals(request.format)) && "1".equals(GZIP_ENABLED) && originalSize > 1024f) {
                try {
                    final ByteArrayOutputStream gzip = gzip(text);
                    gzipSize = gzip.size();
                    sizeReduction = (float) ((originalSize - gzipSize) * 100) / originalSize;
                    if (sizeReduction > 0f) {
                        response.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
                        response.setHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(gzip.size()));
                        response.out = gzip;
                    }
                } catch (IOException ioe) {
                    renderHttpException(new HttpException(APIErrorCode.IO_ERROR, ioe));
                }
            }
        }

        if (ex == null) {
            String ip = request.remoteAddress == null ? InetAddress.getLocalHost().getHostAddress() : request.remoteAddress;
            String referer = request.headers.containsKey(HttpHeaders.REFERER) ? request.headers.get(HttpHeaders.REFERER).value() : StringUtils.EMPTY;
            String useragent = request.headers.containsKey(HttpHeaders.USER_AGENT) ? request.headers.get(HttpHeaders.USER_AGENT).value() : StringUtils.EMPTY;
            Logger.debug("%s - %d %s: [referer = %s, user agent = %s] [request id = %s] [input = %s] [request cookies = %d (%s), response cookies = %d (%s)] [size = %,d, Gzip size = %,d, size reduction = %s%%]",
                ip,
                response.status,
                request.method + " " + request.path,
                referer,
                useragent,
                getRequestId(),
                request.params.allSimple(),
                request.cookies.size(),
                request.cookies.keySet().toString(),
                response.cookies.size(),
                response.cookies.keySet().toString(),
                originalSize,
                gzipSize,
                StringExtensions.toDecimalPlaces(sizeReduction.toString(), 2));
        } else if (ex instanceof HttpException) {
            renderHttpException((HttpException) ex);
        } else if (StringUtils.isNotBlank(ex.getMessage())) {
            renderHttpException(new HttpException(APIErrorCode.INTERNAL_SERVER_ERROR, ex));
        }
    }

    @Util
    public static ByteArrayOutputStream gzip(final String input)
        throws IOException {
        final InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        final ByteArrayOutputStream stringOutputStream = new ByteArrayOutputStream((int) (input.length() * 0.75));
        final OutputStream gzipOutputStream = new GZIPOutputStream(stringOutputStream);

        final byte[] buf = new byte[5000];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            gzipOutputStream.write(buf, 0, len);
        }

        inputStream.close();
        gzipOutputStream.close();

        return stringOutputStream;
    }

    @Util
    public static void renderField(final APIField name, final String value) {
        renderJSON(String.format(JSON_EXPRESSION, name, value));
    }

    @Util
    public static void renderMap(final Map<APIField, String> map) {
        renderJSON(SerializationUtils.serialize(map));
    }

    /**
     * For tracing purposes
     *
     * @return the hashed request id
     */
    @Util
    public static String getRequestId() {
        String userAgent = request.headers.containsKey(HttpHeaders.USER_AGENT) ? request.headers.get(HttpHeaders.USER_AGENT).value() : StringUtils.EMPTY;
        return (session == null) ? StringUtils.EMPTY : Codec.hexSHA1(request.remoteAddress + userAgent);
    }

    /**
     * Error validation
     *
     * @throws HttpException if errors exist
     */
    @Util
    public static void validate() throws HttpException {
        if (Validation.hasErrors()) {
            throw new HttpException(APIErrorCode.VALIDATION_FAILED, validation.errorsMap());
        }
    }

    /**
     * Clear cookies if cookies are off, else remove framework cookies
     */
    @Util
    public static void adjustCookies() {
        boolean cookiesEnabled = Boolean.parseBoolean(Play.configuration.getProperty("cookies.enabled"));
        if (!cookiesEnabled) {
            response.cookies = Maps.newHashMap();
        } else {
            // Remove play cookies
            response.cookies.remove("PLAY_ERRORS");
            response.cookies.remove("PLAY_FLASH");
            response.cookies.remove("PLAY_SESSION");
        }
    }

    @Util
    public static String getPublicIP() {
        if (request != null && request.headers.containsKey(EXTERNAL_IP_HEADER)) {
            return request.headers.get(EXTERNAL_IP_HEADER).value();
        } else {
            try {
                WS.HttpResponse res = WS.url(EXTERNAL_IP_PROVIDER).get();
                return res.getString().replaceAll("\\n", StringUtils.EMPTY).split(",")[0];
            } catch (Exception e) {
                Logger.error("Could not retrieve public IP so defaulting to %s: %s", DEFAULT_EXTERNAL_IP, e.getMessage());
                return DEFAULT_EXTERNAL_IP;
            }
        }
    }

    @Util
    public static String getLocalIP() {
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName(DNS_IP), DNS_IP_PORT);
            return socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException e) {
            Logger.error("Could not retrieve local IP so defaulting to %s: %s", DEFAULT_EXTERNAL_IP, e.getMessage());
            return DEFAULT_EXTERNAL_IP;
        }
    }

    @Util
    public static HttpResponse<Void> httpGet(final String url) throws HttpException {
        return httpRequest(HttpRequest.newBuilder(URI.create(url)).GET(), HttpResponse.BodyHandlers.discarding());
    }

    @Util
    public static HttpResponse<String> httpPost(final String url, final Map<APIField, Object> form, final String apiKey) throws HttpException {
        var body = form.entrySet()
            .stream()
            .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&"));
        var builder = HttpRequest.newBuilder(URI.create(url))
            //.header(HttpHeaders.CONTENT_TYPE, MediaType.FORM_DATA.toString())
            .POST(HttpRequest.BodyPublishers.ofString(body));
        if (apiKey != null) builder.header(API_KEY, apiKey);
        return httpRequest(builder, HttpResponse.BodyHandlers.ofString());
    }

    @Util
    private static <T> HttpResponse<T> httpRequest(final HttpRequest.Builder builder, final HttpResponse.BodyHandler<T> handler) throws HttpException {
        try {
            var request = builder
                .setHeader(HttpHeaders.ACCEPT, MediaType.JSON_UTF_8.toString())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.FORM_DATA.toString())
                .timeout(Duration.ofSeconds(10))
                .build();
            // use the client to send the request
            return HttpClient.newHttpClient().send(request, handler);
        } catch (InterruptedException | IOException e) {
            throw new HttpException(APIErrorCode.STRIPE_EXCEPTION);
        }
    }
}