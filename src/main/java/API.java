import com.google.gson.JsonObject;
import org.apache.http.Header;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class API {
    private String _appID;
    private String _uri;
    private Boolean _debug;

    private CloseableHttpClient _httpclient;
    private BasicCookieStore _cookieStore;

    public API(String appID, Boolean isProduction, Boolean debug) {
        _debug = debug;
        _appID = appID;

        _uri = isProduction ?
                "https://production-api.cardcash.com/v3/" :
                "https://sandbox-api.cardcash.com/v3/";

        List<BasicHeader> defaultHeaders = Arrays.asList(
                new BasicHeader("x-cc-app", _appID)
        );

        _cookieStore = new BasicCookieStore();
        _httpclient = HttpClients.custom()
                .setDefaultCookieStore(_cookieStore)
                .setDefaultHeaders(defaultHeaders)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();
    }

    private boolean myCookieExist() {
        AtomicBoolean foundCCCookie = new AtomicBoolean(false);
        List<Cookie> cookies = _cookieStore.getCookies();

        if (!cookies.isEmpty()) {
            cookies.forEach(c -> {
                if (c.getName().equals(_appID)) {
                    foundCCCookie.set(true);
                }
            });
        }

        return foundCCCookie.get();
    }

    private String Execute(String method, String path, JsonObject body) throws Exception {
        try {
            if (!myCookieExist() && !path.equals("session")) {
                Execute("post", "session", new JsonObject());
            }

            HttpUriRequest request = null;

            switch (method) {
                case "post":
                    HttpEntity entityPost = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);

                    request = RequestBuilder.post()
                            .setUri(new URI(_uri + path))
                            .setEntity(entityPost)
                            .build();
                    break;
                case "put":
                    HttpEntity entityPut = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);

                    request = RequestBuilder.put()
                            .setUri(new URI(_uri + path))
                            .setEntity(entityPut)
                            .build();
                    break;
                case "delete":
                    request = RequestBuilder.delete()
                            .setUri(new URI(_uri + path)).build();
                    break;

                default:
                    request = RequestBuilder.get()
                            .setUri(new URI(_uri + path)).build();
                    break;
            }

            CloseableHttpResponse response = _httpclient.execute(request);

            try {

                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                Header[] headers = response.getAllHeaders();

                if (_debug) {
                    System.out.println();
                    System.out.println("--- REQUEST ---");
                    System.out.println(request.toString());
                    System.out.println("body: " + body.toString());
                    System.out.println("--- RESPONSE HEADERS ---");
                    Arrays.stream(headers).forEach(h -> System.out.println(h));
                    System.out.println("--- RESPONSE ---");
                    System.out.println(response.getStatusLine().toString());
                    System.out.println("result: " + result);
                    System.out.println("--- MY COOKIES ---");
                    List<Cookie> cookies = _cookieStore.getCookies();
                    cookies.forEach(c -> {
                        if (c.getName().equals(_appID)) {
                            System.out.println(c.getValue());
                        }
                    });
                    System.out.println();
                }

                return result;
            } finally {
                response.close();
            }
        } finally {}
    }

    public String CustomerLogin(String email, String password) throws Exception {
        JsonObject data = new JsonObject();
        JsonObject customer = new JsonObject();

        customer.addProperty("email", email);
        customer.addProperty("password", password);

        data.add("customer", customer);

        return Execute("post", "customers/login", data);
    }

    public String GetCustomer() throws Exception {
        return Execute("get", "customers/", new JsonObject());
    }

    public String GetDefaultPaymentOptions() throws Exception {
        return Execute("get", "customers/payment-options", new JsonObject());
    }

    public String GetMerchants() throws Exception {
        return Execute("get", "merchants/sell", new JsonObject());
    }

    public String RetrieveCart() throws Exception {
        return Execute("get", "carts", new JsonObject());
    }

    public String CreateCart() throws Exception {
        JsonObject data = new JsonObject();
        data.addProperty("action", "sell");

        return Execute("post", "carts", data);
    }

    public String DeleteCart(String cartID) throws Exception {
        return Execute("delete", "carts/" + cartID, new JsonObject());
    }

    public String AddCardToCart(String cartID, int merchantID, double cardValue, String cardNum, String cardPin, String refID) throws Exception {
        JsonObject card = new JsonObject();
        JsonObject addCard = new JsonObject();

        addCard.addProperty("merchantId", merchantID);
        addCard.addProperty("enterValue", cardValue);

        if (!cardNum.isEmpty()) {
            addCard.addProperty("number", cardNum);
        }

        if (!cardPin.isEmpty()) {
            addCard.addProperty("pin", cardPin);
        }

        if (!refID.isEmpty()) {
            addCard.addProperty("refId", refID);
        }

        card.add("card", addCard);

        return Execute("post", "carts/" + cartID + "/cards", card);
    }

    public String UpdateCardInCart(String cartID, String cardID, double cardValue, String cardNum, String cardPin, String refID) throws Exception {
        JsonObject card = new JsonObject();
        JsonObject updatedCard = new JsonObject();

        if (cardValue != 0) {
            updatedCard.addProperty("enterValue", cardValue);
        }

        if (!cardNum.isEmpty()) {
            updatedCard.addProperty("number", cardNum);
        }

        if (!cardPin.isEmpty()) {
            updatedCard.addProperty("pin", cardPin);
        }

        if (!refID.isEmpty()) {
            updatedCard.addProperty("refId", refID);
        }

        card.add("card", updatedCard);

        return Execute("put", "carts/" + cartID + "/cards" + cardID, card);
    }

    public String DeleteCardInCart(String cartID, String cardID) throws Exception {
        return Execute("delete", "carts/" + cartID + "/cards" + cardID, new JsonObject());
    }

    public String PlaceOrder(String cartID, int paymentDetailID, String firstName, String lastName, String street, String street2, String city, String state, String postcode) throws Exception {
        JsonObject order = new JsonObject();
        JsonObject paymentDetails = new JsonObject();
        JsonObject billingDetails = new JsonObject();

        paymentDetails.addProperty("id", paymentDetailID);

        billingDetails.addProperty("firstname" , firstName);
        billingDetails.addProperty("lastname" , lastName);
        billingDetails.addProperty("street" , street);
        billingDetails.addProperty("city" , city);
        billingDetails.addProperty("state" , state);
        billingDetails.addProperty("postcode" , postcode);

        if (!street2.isEmpty()) {
            billingDetails.addProperty("street2" , street2);
        }

        order.addProperty("autoSplit", true);
        order.addProperty("cartId", cartID);
        order.addProperty("paymentMethod", "ACH_BULK");
        order.add("billingDetails", billingDetails);
        order.add("paymentDetails", paymentDetails);

        return Execute("post", "orders", order);
    }

    public String GetOrder(String orderID) throws Exception {
        return Execute("get", "orders/" + orderID, new JsonObject());
    }

    public String GetAllOrders() throws Exception {
        return Execute("get", "orders/sell", new JsonObject());
    }

    public String GetOrderCards(String orderID) throws Exception {
        return Execute("get", "orders/sell?orderId=" + orderID, new JsonObject());
    }

    public String GetAllCards() throws Exception {
        return Execute("get", "cards/sell", new JsonObject());
    }

    public String GetAllPayments() throws Exception {
        return Execute("get", "payments/sell", new JsonObject());
    }

    public String GetPayment(String paymentID) throws Exception {
        return Execute("get", "payments/sell/" + paymentID, new JsonObject());
    }
}
