package functional;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import controllers.Customers;
import enums.APIField;
import interfaces.Mockable;
import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import play.mvc.Http;

/**
 * CustomersTest
 *
 * @see CustomersTest
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CustomersTest extends AbstractFunctionalTest {
    private static String id;

    /**
     * GET      /categories
     *
     * @see Customers#create(String, String, String)
     */
    @Test
    public void stage01_create() {
        ParamBuilder builder = new ParamBuilder()
            .param(APIField.CUSTOMER, Mockable.stringAlphaNumUpper(6))
            .param(APIField.EMAIL, Mockable.emails())
            .param(APIField.NAME, Mockable.namesNullable());
        Http.Response response = POST(builder.build(), createURI("/customers"));
        id = getIdFromResponse(response).get();
        assertIsSuccess(response);
    }

    /**
     * GET      /categories/popular
     *
     * @see Customers#get(String)
     */
    @Test
    public final void stage02_get() {
        Http.Response response = GET(createURI("/customers/" + id));
        assertIsSuccess(response);
    }

    /**
     * GET      /categories
     *
     * @see Customers#getAll(String, String, Long, Integer)
     */
    @Test
    public final void stage03_getAll() {
        Http.Response response = GET(createURI("/customers"));
        assertIsSuccess(response);
    }
}

