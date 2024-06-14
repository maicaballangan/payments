package controllers;

import com.google.common.net.HttpHeaders;
import com.google.gson.JsonSyntaxException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import constants.Config;
import enums.APIErrorCode;
import enums.APIField;
import exceptions.HttpException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.groovy.util.Maps;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Util;
import play.mvc.With;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Webhooks
 *
 * @author Maica Ballangan
 * @since v1
 */
@With(APIHelpers.class)
public class Webhooks extends Controller {
    private static final String SIGNATURE = "stripe-signature";

    /**
     * POST      /webhooks
     */
    public static void create() throws HttpException {
        Logger.info("Webhook transaction received");
        Event event;

        try {
            event = Webhook.constructEvent(IOUtils.toString(request.body, StandardCharsets.UTF_8.name()),
                    request.headers.get(SIGNATURE).value(), Config.STRIPE_WEBHOOK_KEY);
        } catch (IOException | JsonSyntaxException e) {
            throw new HttpException(APIErrorCode.INVALID_PAYLOAD);
        } catch (SignatureVerificationException e) {
            throw new HttpException(APIErrorCode.INVALID_SIGNATURE);
        }

        switch (event.getType()) {
            case "payment_intent.succeeded":
                var transaction = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElseThrow(() -> new HttpException(APIErrorCode.EVENT_TYPE_ERROR));
                Logger.info("Session id: %s", session.getId());
                // Save an order in your database, marked as 'awaiting payment'
                // createOrder(session);

                // Check if the order is paid (e.g., from a card payment)
                //
                // A delayed notification payment will have an `unpaid` status, as
                // you're still waiting for funds to be transferred from the customer's
                // account.
                if (transaction.getStatus().equals("succeeded")) {
                    // Fulfill the purchase
                    response.status = fulfillOrder(transaction);
                } else {
                    // TODO: Mark order as 'processing payment'
                }
                break;
            case "payment_intent.failed":
                // TODO call /failed
                break;
            default:
                throw new HttpException(APIErrorCode.EVENT_TYPE_ERROR);
        }
        Logger.info("Webhook transaction done");
    }

    @Util
    public static int fulfillOrder(PaymentIntent transaction) throws HttpException {
        Logger.info("Payment success (Transaction ID: %s)", transaction.getId());
        var origin = transaction.getMetadata().get(HttpHeaders.ORIGIN.toLowerCase());
        if (StringUtils.isBlank(origin)) {
            throw new HttpException(APIErrorCode.ORIGIN_MISSING);
        }
        var response = APIHelpers.httpPost(origin + "/orders/pay", Maps.of(APIField.TRANSACTION, transaction.getId(), APIField.AMOUNT, transaction.getAmountReceived()), Config.getKey(origin));
        return response.statusCode();
    }
}
