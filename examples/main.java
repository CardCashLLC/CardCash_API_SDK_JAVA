import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.cardcash.API

public class main {


    public static void main(String[] args) throws Exception {
        Gson gson = new Gson();
        API ccAPI = new API("", false, false);


        String CustomerLogin = ccAPI.CustomerLogin("", "");
        System.out.println("CustomerLogin " + CustomerLogin);

        String GetCustomer = ccAPI.GetCustomer();
        System.out.println("Customer Profile " + GetCustomer);

        String GetMerchants = ccAPI.GetMerchants();
        System.out.println("GetMerchants " + GetMerchants);

        String RetrieveCart = ccAPI.RetrieveCart();
        System.out.println("RetrieveCart " + RetrieveCart);

        String CreateCart = ccAPI.CreateCart();
        System.out.println("CreateCart " + CreateCart);

        Map<String, Object> cart = gson.fromJson(CreateCart, new TypeToken<Map<String, Object>>() {}.getType());
        String cartID = cart.get("cartId").toString();

        String AddCart = ccAPI.AddCardToCart(cartID, , , "", "", "");
        System.out.println("AddCart " + AddCart);

        String GetDefaultPaymentOptions = ccAPI.GetDefaultPaymentOptions();
        System.out.println("GetDefaultPaymentOptions " + GetDefaultPaymentOptions);
        Map<String, Object> paymentOptions = gson.fromJson(GetDefaultPaymentOptions, new TypeToken<Map<String, Object>>() {}.getType());
        Map<String, Object> PaymentOptionsDataSell = gson.fromJson(paymentOptions.get("data").toString(), new TypeToken<Map<String, Object>>() {}.getType());
        Map<String, Object> PaymentOptionsDataSellItem = gson.fromJson(PaymentOptionsDataSell.get("sell").toString(), new TypeToken<Map<String, Object>>() {}.getType());
        List<PaymentOptionsList> myObjects = Arrays.asList(gson.fromJson(PaymentOptionsDataSellItem.get("items").toString(), PaymentOptionsList[].class));
        PaymentOptionsList item0 = myObjects.get(0);
        PaymentDetailsDetails paymentId = gson.fromJson(item0.getPaymentDetails().toString(), PaymentDetailsDetails.class);
        Integer defaultPaymentID = paymentId.getId();


        String PlaceOrder = ccAPI.PlaceOrder(cartID, defaultPaymentID, "", "", "", "","", "", "" );
        System.out.println("PlaceOrder " + PlaceOrder);

        String GetAllOrders = ccAPI.GetAllOrders();
        System.out.println("GetAllOrders " + GetAllOrders);

        String GetAllPayments = ccAPI.GetAllPayments();
        System.out.println("GetAllPayments " + GetAllPayments);
    }
}
