package constants;

import com.google.common.collect.Maps;
import com.stripe.Stripe;
import controllers.APIHelpers;
import edu.nyu.cs.javagit.api.DotGit;
import enums.APIField;
import exceptions.HttpException;
import play.Play;
import play.libs.Time;
import play.vfs.VirtualFile;
import utils.SerializationUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Config {

    static {
        Stripe.apiKey = Play.configuration.getProperty("stripe.secret.key");
    }

    private Config() {
        throw new IllegalStateException("Utility class");
    }

    public static final String APPLICATION_NAME = Play.configuration.getProperty("application.name").toLowerCase();
    public static final String ATTACHMENTS_PATH = Play.configuration.getProperty("attachments.path");
    public static final String AUTH_ID = Play.configuration.getProperty("auth.id");
    public static final String AUTH_PASS = Play.configuration.getProperty("auth.password");
    private static final String PAYMENTS_ID = Play.configuration.getProperty("payments.id");
    private static final String PAYMENTS_PASS = Play.configuration.getProperty("payments.password");
    public static final int HEARTBEAT_DURATION = Time.parseDuration(Play.configuration.getProperty("application.heartbeat"));
    public static final String TIMEZONE = Play.configuration.getProperty("application.timezone");
    public static final String COUNTRY = Play.configuration.getProperty("application.country");
    public static final String VERSION = Play.configuration.getProperty("application.version");
    public static final String API_VERSION = Play.configuration.getProperty("api.version");
    public static final String API_URL = Play.configuration.getProperty("api.address");
    public static final String API_PORT = Play.configuration.getProperty("http.port");
    public static final String API_URL_SCHEME = Play.configuration.getProperty("api.address.scheme");
    public static final String WS_URL_SCHEME = Play.configuration.getProperty("api.address.ws");
    public static final String BASE_URL = String.format("%s/%s", API_URL, VERSION);
    public static final String BASE_URL_SCHEME = String.format("%s%s", API_URL_SCHEME, BASE_URL);
    public static final String WS_URL = String.format("%s%s", WS_URL_SCHEME, BASE_URL);
    public static final boolean IS_SECURE = !"http://".equals(API_URL_SCHEME)
        || !API_URL.startsWith("localhost");
    public static final DotGit DOT_GIT = DotGit.getInstance(VirtualFile.fromRelativePath(Character.toString(Constants.DELIMITER_PATH)).getRealFile().getAbsolutePath());

    // Stripe
    public static final String MAC_ADDRESS = Play.configuration.getProperty("mac.address");
    public static final String STRIPE_API_KEY = Play.configuration.getProperty("stripe.secret.key");
    public static final String STRIPE_WEBHOOK_KEY = Play.configuration.getProperty("stripe.webhook.signing.secret");
    public static final List<String> REFERERS = Arrays.asList(Play.configuration.getProperty("allowed.referers").split(Constants.DELIMITER_COMMA));

    // Git
    public static final String GIT_URL = Play.configuration.getProperty("git.address");
    public static final String GIT_URL_USER = Play.configuration.getProperty("git.address.user");
    public static final String GIT_URL_PASSWORD = Play.configuration.getProperty("git.address.password");

    // API keys
    private static final Map<String, String> KEYS = Maps.newHashMap();

    /**
     * Get api key using the site's url
     *
     * @param url
     * @return api key
     * @throws HttpException
     */
    public static String getKey(final String url) throws HttpException {
        if (KEYS.containsKey(url)) {
            return KEYS.get(url);
        } else {
            var response = APIHelpers.httpPost(url + "/sessions/current", Map.of(APIField.USERNAME, PAYMENTS_ID, APIField.PASSWORD, PAYMENTS_PASS), null);
            var key = ((Session) SerializationUtils.deserialize(response.body(), Session.class)).getSession();
            KEYS.put(url, key);
            return key;
        }
    }

    private static class Session {
        private String session;

        public String getSession() {
            return session;
        }

        public void setSession(String session) {
            this.session = session;
        }
    }
}
