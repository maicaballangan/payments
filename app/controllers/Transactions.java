package controllers;

import com.google.common.net.HttpHeaders;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import constants.Config;
import enums.APIErrorCode;
import enums.APIField;
import exceptions.HttpException;
import org.apache.commons.lang.StringUtils;
import org.apache.groovy.util.Maps;
import play.data.validation.Min;
import play.data.validation.Required;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;

import static com.stripe.param.PaymentIntentCreateParams.SetupFutureUsage.OFF_SESSION;
import static com.stripe.param.PaymentMethodCreateParams.Type.CARD;

/**
 * Transactions
 *
 * @author Maica Ballangan
 * @since v1
 */
@With(APIHelpers.class)
public class Transactions extends Controller {

    /**
     * Referer-based Authentication from HTTP Header
     */
    @Before
    static void authenticate() throws HttpException {
        APIHelpers.authenticate();
    }

    /**
     * GET      /transactions/{id}
     *
     * @param id path
     * @throws StripeException     500
     */
    public static void get(@Required  final String id) throws StripeException {
        renderJSON(PaymentIntent.retrieve(id));
    }

    /**
     * POST     /transactions
     *
     * @param ccy           form
     * @param amount        form
     * @param email         form
     * @param order         form
     * @param customer      form
     * @throws StripeException
     */
    public static void create(@Required final String ccy,
                              @Required @Min(1) BigDecimal amount,
                              @Required final String customer,
                              @Required final String order,
                              final String email) throws StripeException, HttpException {
        String origin = request.headers.get(HttpHeaders.ORIGIN.toLowerCase()).value();
        if (StringUtils.isBlank(origin)) {
            throw new HttpException(APIErrorCode.ORIGIN_MISSING);
        }

        var params = new PaymentIntentCreateParams.Builder()
            .setCurrency(ccy)
            .setAmount(amount.longValue())
            .setCustomer(customer)
            .setReceiptEmail(email)
            .addPaymentMethodType(CARD.getValue())
            .setSetupFutureUsage(OFF_SESSION) // Save payment info for future fees like cancellation/no show
            .putMetadata(HttpHeaders.ORIGIN.toLowerCase(), origin)
            .putMetadata(APIField.ORDER.toString(), order)
            .build();

        var options = RequestOptions.builder()
            //.setClientId(clientId)
            .setApiKey(Config.STRIPE_API_KEY)
            .setIdempotencyKey(order)
            .build();
        var transaction = PaymentIntent.create(params, options);
        var map = Maps.of(APIField.ID, transaction.getId(), APIField.CLIENT_SECRET, transaction.getClientSecret());
        response.status = 201;
        APIHelpers.renderMap(map);
    }
}