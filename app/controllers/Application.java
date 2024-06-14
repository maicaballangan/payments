package controllers;

import com.google.gson.JsonElement;
import constants.Config;
import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.commands.GitLogOptions;
import edu.nyu.cs.javagit.api.commands.GitLogResponse;
import enums.APIErrorCode;
import enums.MimeType;
import exceptions.HttpException;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.libs.WS;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Finally;
import play.mvc.Util;

import java.io.IOException;

import static controllers.APIHelpers.cacheHeaders;

/**
 * Main application entry point - used to define basic authentication and documentation interface
 *
 * @author Maica Ballangan
 * @since v1
 */
public class Application extends Controller {

    public static final String ERROR_PAGE = "Application/error.json";
    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    private static final String REALM = "Basic realm=\"Payments\"";

    /**
     * GET      /info
     */
    public static void info() throws HttpException {
        try {
            JsonElement json = WS.url(Config.GIT_URL).authenticate(Config.GIT_URL_USER, Config.GIT_URL_PASSWORD)
                .setParameter("until", "master")
                .setParameter("limit", "1")
                .setHeader("Content-Type", MimeType.json.contentType())
                .get().getJson();
            String serverSHA = (json.getAsJsonObject().getAsJsonArray("values") != null) ?
                json.getAsJsonObject().getAsJsonArray("values").get(0).getAsJsonObject().get("displayId").getAsString() : StringUtils.EMPTY;
            GitLogOptions options = new GitLogOptions();
            options.setOptLimitCommitMax(true, 1);
            options.setOptLimitComitUntil(true, "master");
            GitLogResponse.Commit c = Config.DOT_GIT.getLog(options).get(0);
            String currentSHA = c.getSha().substring(0, 11);
            String lastUpdated = c.getDateString();
            String applicationName = Config.APPLICATION_NAME;
            String attachmentsPath = Config.ATTACHMENTS_PATH;
            String release = Config.VERSION;
            String apiVersion = Config.API_VERSION;
            String javaVersion = System.getProperty("java.version");
            String javaVendor = System.getProperty("java.vendor");
            String frameworkVersion = Play.version;
            String url = Config.BASE_URL_SCHEME;
            String env = Play.id;
            String envType = Play.mode.toString();
            String logLevel = Logger.log4j.getLevel().toString();
            String logFile = Play.configuration.getProperty("application.log.path");
            String service = Application.getServiceRegistryUrl();
            cacheHeaders("6h");
            response.setContentTypeIfNotSet(MimeType.json.contentType());
            request.format = MimeType.json.name();
            render(applicationName, attachmentsPath, release, apiVersion, javaVersion, javaVendor, frameworkVersion, url, serverSHA,
                currentSHA, env, envType, logLevel, logFile, service, lastUpdated);
        } catch (JavaGitException | IOException e) {
            throw new HttpException(APIErrorCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Before(unless = {"notFound", "ping", "heartbeat", "info"})
    static void authenticate() {
        if (!(Config.AUTH_ID.equals(request.user) && Config.AUTH_PASS.equals(request.password))) {
            response.setHeader(WWW_AUTHENTICATE, REALM);
            request.format = MimeType.json.name();
            HttpException e = new HttpException(APIErrorCode.BASIC_AUTHENTICATION_FAILED);
            response.status = e.getHttpStatusCode();
            render(ERROR_PAGE, e);
        }
    }

    @Finally
    protected static void removeCookies() {
        APIHelpers.adjustCookies();
    }

    @Util
    public static String getServiceRegistryUrl() {
        return APIHelpers.getLocalIP() + "_" + Config.API_PORT;
    }

    /**
     * *       /.*
     */
    public static void notFound() {
        request.format = MimeType.json.name();
        HttpException e = new HttpException(APIErrorCode.RESOURCE_NOT_FOUND, request.method + " " + request.path);
        response.status = e.getHttpStatusCode();
        render(Application.ERROR_PAGE, e);
    }

    /**
     * GET /
     */
    public static void index() {
        String discoveryURL = Config.BASE_URL_SCHEME + "/resources.json";
        render(discoveryURL);
    }

    /**
     * GET     /resources.json
     */
    public static void resources() {
        render("Application/resources.json");
    }

    /**
     * GET     /resources/{api}
     *
     * @param api path
     */
    public static void resource(final String api) {
        render("Application/resources/" + api);
    }

    /**
     * GET      /ping
     */
    public static void ping() {
        ok();
    }

    /**
     * GET     /heartbeat
     *
     * @return the number of heartbeats since startup
     */
    /*public static long heartbeat() {
        return Heartbeat.getHeartbeat();
    }*/
}