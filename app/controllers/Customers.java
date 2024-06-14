package controllers;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerListParams;
import constants.Config;
import constants.Constants;
import enums.APIField;
import exceptions.HttpException;
import play.data.validation.Max;
import play.data.validation.Min;
import play.data.validation.Required;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

/**
 * Customer
 *
 * @author Maica Ballangan
 * @since v1
 */
@With(APIHelpers.class)
public class Customers extends Controller {

    /**
     * Referer-based Authentication from HTTP Header
     */
    @Before
    static void authenticate() throws HttpException {
        APIHelpers.authenticate();
    }

    /**
     * GET      /customers
     *
     * @param email path
     * @param after path
     * @param size  path
     * @param page  path
     * @throws StripeException 500
     */
    public static void getAll(final String email,
                              final String after,
                              @Min(0) @Max(100) final Long size,
                              @Min(1) final Integer page) throws StripeException {
        var response = Customer.list(CustomerListParams.builder()
            .setEmail(email)
            .setLimit(size != null ? size : Constants.RESULTS_SIZE)
            .setStartingAfter(after)
            .build());
        var results = response.getData();
        boolean hasMore = response.getHasMore();
        render(APIHelpers.GET_ALL_TEMPLATE, page, hasMore, results);
    }

    /**
     * GET      /customers/{id}
     *
     * @param id path
     * @throws StripeException 500
     */
    public static void get(@Required final String id) throws StripeException {
        renderJSON(Customer.retrieve(id));
    }

    public static void create(@Required String email, String name, String customer) throws StripeException {
        var params = CustomerCreateParams.builder()
            .setEmail(email)
            .setName(name)
            .putMetadata(APIField.CUSTOMER.field(), customer)
            .build();
        var options = RequestOptions.builder()
            .setApiKey(Config.STRIPE_API_KEY)
            .setIdempotencyKey(email) // Makes sure that email is registered only once
            .build();
        String id = Customer.create(params, options).getId();
        response.status = 201;
        APIHelpers.renderField(APIField.ID, id);
    }
}
