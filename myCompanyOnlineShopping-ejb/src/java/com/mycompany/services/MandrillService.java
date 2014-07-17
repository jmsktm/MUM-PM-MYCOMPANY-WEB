/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.services;

import com.mandrill.clients.exception.RequestFailedException;
import com.mandrill.clients.model.MandrillMessage;
import com.mandrill.clients.model.MandrillRecipient;
import com.mandrill.clients.model.MandrillTemplatedMessageRequest;
import com.mandrill.clients.model.MergeVar;
import com.mandrill.clients.request.MandrillMessagesRequest;
import com.mandrill.clients.request.MandrillRESTRequest;
import com.mandrill.clients.util.MandrillConfiguration;
import com.mycompany.models.PurchaseOrder;
import com.mycompany.models.ShoppingCartItem;
import com.mycompany.models.Users;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * This class handles SMTP facility
 *
 * @author james version 1.0.0
 */
@Stateless
public class MandrillService {

    @PersistenceContext
    private EntityManager em;

    private static MandrillRESTRequest request;
    private static MandrillConfiguration config;
    private static MandrillMessagesRequest messagesRequest;
    private static HttpClient client;
    private static ObjectMapper mapper;
    private static Properties props;

    private final static String TEMPLATE_USER_REGISTERED = "user-registered";

    private final static String TEMPLATE_PURCHASE_ORDER = "Orderdetail";

    private final static String TAG_REGISTRATION = "registration";
    private final static String TAG_USER_ACCOUNT = "useraccount";

    private final static String TAG_ORDER = "order";
    private final static String TAG_SALES = "Sales";

    private static boolean initialized = false;

    /**
     * This method initializes all the methods
     *
     */
    private void initialize() {
        if (!initialized) {
            try {
                request = new MandrillRESTRequest();
                config = new MandrillConfiguration();
                messagesRequest = new MandrillMessagesRequest();
                mapper = new ObjectMapper();
                props = new Properties();

                config.setApiKey("1m627Fh2pGIKLeweZAUfjg");
                config.setApiVersion("1.0");
                config.setBaseURL("https://mandrillapp.com/api");
                request.setConfig(config);
                request.setObjectMapper(mapper);
                messagesRequest.setRequest(request);

                client = new DefaultHttpClient();
                request.setHttpClient(client);

                initialized = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method sends e-mail
     *
     * @param mandrillTemplatedMessageRequest
     * @throws RequestFailedException
     */

    public void sendTemplatedMessage(MandrillTemplatedMessageRequest mandrillTemplatedMessageRequest) throws RequestFailedException {
        initialize();
        messagesRequest.sendTemplatedMessage(mandrillTemplatedMessageRequest);
    }

    /**
     * This method request for templated SMTP.
     *
     * @param toEmailList
     * @param user
     * @param subject
     * @return
     */

    public MandrillTemplatedMessageRequest getMandrillMessageObject(ArrayList<String> toEmailList,
            Users user, String subject) {
        initialize();
        if (user != null && user.getEmail() != null && user.getEmail().length() > 0) {
            MandrillTemplatedMessageRequest mandrillTemplatedMessageRequest = new MandrillTemplatedMessageRequest();
            mandrillTemplatedMessageRequest.setTemplate_name(TEMPLATE_USER_REGISTERED);
            MandrillMessage message = new MandrillMessage();

            int size = 1;
            if (toEmailList != null) {
                size += toEmailList.size();
            }
            MandrillRecipient[] toEmailArray = new MandrillRecipient[size];
            toEmailArray[0] = new MandrillRecipient("", user.getEmail());

            if (toEmailList != null) {
                for (int i = 1; i < toEmailList.size(); i++) {
                    toEmailArray[i] = new MandrillRecipient("", toEmailList.get(i));
                }
            }
            message.setTo(toEmailArray);
            message.setSubject(subject);

            message.setTags(new String[]{TAG_REGISTRATION, TAG_USER_ACCOUNT});

            List<MergeVar> globalMergeVars = new ArrayList<MergeVar>();
            globalMergeVars.add(new MergeVar("NAME", user.getFirstName()));

            message.setGlobal_merge_vars(globalMergeVars);
            mandrillTemplatedMessageRequest.setMessage(message);
            return mandrillTemplatedMessageRequest;
        } else {
            return null;
        }
    }

    public MandrillTemplatedMessageRequest getMandrillOrderMessageObject(ArrayList<String> toEmailList,
            PurchaseOrder order, String subject) {
        initialize();
        if (order != null && order.getUser() != null && order.getUser() != null && order.getUser().getEmail().length() > 0) {
            MandrillTemplatedMessageRequest mandrillTemplatedMessageRequest = new MandrillTemplatedMessageRequest();
            mandrillTemplatedMessageRequest.setTemplate_name(TEMPLATE_PURCHASE_ORDER);
            MandrillMessage message = new MandrillMessage();

            int size = 1;
            if (toEmailList != null) {
                size += toEmailList.size();
            }
            MandrillRecipient[] toEmailArray = new MandrillRecipient[size];
            toEmailArray[0] = new MandrillRecipient("", order.getUser().getEmail());

            if (toEmailList != null) {
                for (int i = 1; i < toEmailList.size(); i++) {
                    toEmailArray[i] = new MandrillRecipient("", toEmailList.get(i));
                }
            }
            message.setTo(toEmailArray);
            message.setSubject(subject);

            message.setTags(new String[]{TAG_ORDER, TAG_SALES});

            List<MergeVar> globalMergeVars = new ArrayList<MergeVar>();
            globalMergeVars.add(new MergeVar("CustName", order.getUser().getFirstName() + " " + order.getUser().getLastName()));
            globalMergeVars.add(new MergeVar("OrderDate", new SimpleDateFormat("MM/dd/yyyy").format(order.getBuyingDate().getTime())));
            globalMergeVars.add(new MergeVar("OrderId", String.valueOf(order.getId())));

            String detail = "<table width='100%'><tr><td>Product</td><td>Quantity</td><td>Price</td></tr>";
            for (ShoppingCartItem item : order.getShoppingCart().getShoppingCartItems()) {
                detail += "<tr><td>" + item.getProduct().getName() + "</td><td>" + item.getQuantity() + "</td><td>" + item.getPrice() + "</td></tr>";
            }
            detail += "</table>";

            globalMergeVars.add(new MergeVar("OrderDetail", detail));

            message.setGlobal_merge_vars(globalMergeVars);
            mandrillTemplatedMessageRequest.setMessage(message);
            return mandrillTemplatedMessageRequest;
        } else {
            return null;
        }
    }
}
