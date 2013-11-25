package lukasync.client;

import java.util.List;

import lukasync.Lukasync;
import lukasync.magentoclient.AssociativeArray;
import lukasync.magentoclient.AssociativeEntity;
import lukasync.magentoclient.ComplexFilter;
import lukasync.magentoclient.ComplexFilterArray;
import lukasync.magentoclient.CustomerCustomerCreateRequestParam;
import lukasync.magentoclient.CustomerCustomerCreateResponseParam;
import lukasync.magentoclient.CustomerCustomerEntityToCreate;
import lukasync.magentoclient.Filters;
import lukasync.magentoclient.LoginParam;
import lukasync.magentoclient.LoginResponseParam;
import lukasync.magentoclient.MageApiModelServerWsiHandlerPortType;
import lukasync.magentoclient.MagentoService;
import lukasync.magentoclient.ObjectFactory;
import lukasync.magentoclient.SalesOrderListEntity;
import lukasync.magentoclient.SalesOrderListEntityArray;
import lukasync.magentoclient.SalesOrderListRequestParam;
import lukasync.magentoclient.SalesOrderListResponseParam;
import lukasync.util.JSONUtil;

import org.json.JSONArray;
import org.json.JSONObject;

public class MagentoClient extends ServiceClient {
    private MagentoService magentoService;
    private MageApiModelServerWsiHandlerPortType port;
    private String sessionId;

    public MagentoClient(JSONObject conf) {
        super(conf);
    }

    @Override
    protected void init() {
        magentoService = new MagentoService();
        port = magentoService.getMageApiModelServerWsiHandlerPort();

        LoginParam loginRequestParam = new LoginParam();
        loginRequestParam.setUsername(username);
        loginRequestParam.setApiKey(password);

        LoginResponseParam loginResponseParam = port.login(loginRequestParam);
        sessionId = loginResponseParam.getResult();

        if (Lukasync.PRINT_DEBUG) {
            System.out.println("DEBUG: sessionId : " + sessionId);
        }
    }

    public int createCustomer (JSONObject customer) {
        if (Lukasync.PRINT_DEBUG) {
            System.out.println("\nDEBUG: MagentoClient creating customer:");
            JSONUtil.prettyPrint(customer);
        }

        return createCustomer(
                1,
                1,
                6, // Sellers user group on organi TODO change this.
                customer.getString("emailAddress"),
                customer.getString("password"),
                customer.getString("firstName"),
                customer.getString("lastName")
        );
    }

    public int createCustomer (
            int websiteId,
            int storeId,

            int userGroupId,
            String email,
            String password,
            String firstName,
            String lastName
    ) {
        ObjectFactory of = new ObjectFactory();
        CustomerCustomerEntityToCreate entityToCreate = of.createCustomerCustomerEntityToCreate();

        entityToCreate.setStoreId(storeId);
        entityToCreate.setWebsiteId(websiteId);
        entityToCreate.setGroupId(userGroupId);

        entityToCreate.setEmail(email);
        entityToCreate.setPassword(password);
        entityToCreate.setFirstname(firstName);
        entityToCreate.setLastname(lastName);

        CustomerCustomerCreateRequestParam requestParam = of.createCustomerCustomerCreateRequestParam();
        requestParam.setCustomerData(entityToCreate);
        requestParam.setSessionId(sessionId);
        CustomerCustomerCreateResponseParam responseParam = port.customerCustomerCreate(requestParam);

        return responseParam.getResult();
    }

    public JSONArray getNewSales(int customerGroupId, String createdAt) {
        ObjectFactory of = new ObjectFactory();

        Filters filters = getNewSalesFilters(customerGroupId, createdAt, of);

        SalesOrderListRequestParam requestParam = of.createSalesOrderListRequestParam();
        requestParam.setFilters(filters);
        requestParam.setSessionId(sessionId);

        SalesOrderListResponseParam responseParam = port.salesOrderList(requestParam);
        SalesOrderListEntityArray entityArray = responseParam.getResult();

        if (entityArray != null) {
            JSONArray unmarshalledEntities = unmarshallResult(entityArray);

            if (Lukasync.PRINT_DEBUG) {
                System.out.println("getNewSales: ");
                JSONUtil.prettyPrint(unmarshalledEntities);
            }

            return unmarshalledEntities;
        } else {
            return null;
        }
    }

    private Filters getNewSalesFilters (int customerGroupId, String createdAt, ObjectFactory of) {
        // filter for customerGroupId
        AssociativeArray filterArr = of.createAssociativeArray();
        List<AssociativeEntity> filterArrList = filterArr.getComplexObjectArray();

        AssociativeEntity customerGroupIdAssociativeEntity = of.createAssociativeEntity();
        customerGroupIdAssociativeEntity.setKey("customer_group_id");
        customerGroupIdAssociativeEntity.setValue("" + customerGroupId);
        filterArrList.add(customerGroupIdAssociativeEntity);

        // complex filter for updateTime
        ComplexFilterArray complexFilterArr = of.createComplexFilterArray();
        List<ComplexFilter> complexFilterArrList = complexFilterArr.getComplexObjectArray();

        AssociativeEntity createdAtAssociativeEntity = of.createAssociativeEntity();
        createdAtAssociativeEntity.setKey("gt");
        createdAtAssociativeEntity.setValue(createdAt);

        ComplexFilter createdAtComplexFilter = of.createComplexFilter();
        createdAtComplexFilter.setKey("created_at");
        createdAtComplexFilter.setValue(createdAtAssociativeEntity);
        complexFilterArrList.add(createdAtComplexFilter);

        // combine regular and complex filters
        Filters filters = of.createFilters();
        filters.setFilter(filterArr);
        filters.setComplexFilter(complexFilterArr);
        return filters;
    }

    private JSONArray unmarshallResult (SalesOrderListEntityArray entityArray) {
        JSONArray result = new JSONArray();

        List<SalesOrderListEntity> salesOrderListEntities = entityArray.getComplexObjectArray();
        if (salesOrderListEntities.size() > 0) {

            for (SalesOrderListEntity e : salesOrderListEntities) {
                String operatorName = e.getCustomerEmail().split("@")[0];
                String gross = e.getGrandTotal();// getTotalInvoiced(); // TODO is this what we want?
                String createdAt = e.getCreatedAt();

                JSONObject eJSON = new JSONObject();
                eJSON.put("username", operatorName);
                eJSON.put("gross", gross);
                eJSON.put("createdAt", createdAt);

                result.put(eJSON);
            }
        }

        return result;
    }

}