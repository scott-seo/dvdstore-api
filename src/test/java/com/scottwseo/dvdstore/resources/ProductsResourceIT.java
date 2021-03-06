package com.scottwseo.dvdstore.resources;

import com.scottwseo.commons.rest.app.APIConfiguration;
import com.scottwseo.dvdstore.DVDStoreAPIConfiguration;
import com.scottwseo.dvdstore.TestSuiteIT;
import com.scottwseo.dvdstore.api.Product;
import com.scottwseo.dvdstore.api.ProductCreate;
import com.scottwseo.dvdstore.api.Products;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertTrue;

/**
 * Created by seos on 10/5/16.
 */
public class ProductsResourceIT {

    private static Logger LOG = LoggerFactory.getLogger(ProductsResourceIT.class);

    private static final String API_URL = "http://localhost:%d/api/v1/dvdstore/products";

    @ClassRule
    public static final DropwizardAppRule<DVDStoreAPIConfiguration> RULE = TestSuiteIT.DROPWIZARD;

    private static Client client = TestSuiteIT.CLIENT;

    @Test
    public void addProduct() throws Exception {

        ProductCreate product = new ProductCreate();
        product.actor("Scott");
        product.category(1L);
        product.title("Scott Goes to Hollywood");
        product.commonProdId(1L);
        product.price(new BigDecimal(10.00));

        Response response = client.target(String.format(API_URL, RULE.getLocalPort())).request().post(Entity.json(product));

        int status = response.getStatus();

        if (status != 200) {
            String responseBody = response.readEntity(String.class);
            LOG.warn(responseBody);
        }

        assertThat(status, is(200));

        Product productReceived = response.readEntity(Product.class);

        assertThat(productReceived, is(notNullValue()));

    }

    @Test
    public void addProductError() throws Exception {

        ProductCreate product = new ProductCreate();
        product.actor("Scott 000000000000000000000000000000000000000000000000000000000");
        product.category(1L);
        product.title("Scott Goes to Hollywood");
        product.commonProdId(1L);
        product.price(new BigDecimal(10.00));

        Response response = client.target(String.format(API_URL, RULE.getLocalPort())).request().post(Entity.json(product));

        int status = response.getStatus();

        assertThat(status, is(400));

        Map error = response.readEntity(Map.class);

        assertThat(error, is(notNullValue()));

    }

    @Test
    public void listProducts() throws Exception {

        Response response = client.target(String.format(API_URL + "?start=1&size=10", RULE.getLocalPort())).request().get();

        int status = response.getStatus();

        if (status != 200) {
            String responseBody = response.readEntity(String.class);
            LOG.warn(responseBody);
        }

        assertThat(status, is(200));

        Products productsReceived = response.readEntity(Products.class);

        assertThat(productsReceived, is(notNullValue()));

    }

    @Test
    public void listProductsValidationError() throws Exception {

        Response response = client.target(String.format(API_URL, RULE.getLocalPort())).request().get();

        int status = response.getStatus();

        assertThat(status, is(400));

        Map error = response.readEntity(Map.class);

        assertThat(error, is(notNullValue()));

    }

    @Test
    public void listProductsError() throws Exception {

        Response response = client.target(String.format(API_URL + "?start=-1&size=10", RULE.getLocalPort())).request().get();

        int status = response.getStatus();

        assertThat(status, is(400));

        Map error = response.readEntity(Map.class);

        assertThat(error, is(notNullValue()));

    }


    @Test
    public void updateProduct() throws Exception {

        ProductCreate product = createProduct();

        ProductCreate productUpdate = new ProductCreate();
        productUpdate.setProdId(product.getProdId());
        productUpdate.actor("Tom");
        productUpdate.category(2L);
        productUpdate.title("Tom Goes to Hollywood");
        productUpdate.commonProdId(2L);
        productUpdate.price(new BigDecimal(20.00));

        Response response = client.target(String.format(API_URL, RULE.getLocalPort())).request().put(Entity.json(productUpdate));

        int status = response.getStatus();

        assertThat(status, is(204));
    }

    @Test
    public void updateProductError() throws Exception {

        ProductCreate product = createProduct();

        ProductCreate productUpdate = new ProductCreate();
        productUpdate.setProdId(product.getProdId());
        productUpdate.actor("Tom 000000000000000000000000000000000000000000000000000000000000000000000000000");
        productUpdate.category(2L);
        productUpdate.title("Tom Goes to Hollywood");
        productUpdate.commonProdId(2L);
        productUpdate.price(new BigDecimal(20.00));

        Response response = client.target(String.format(API_URL, RULE.getLocalPort())).request().put(Entity.json(productUpdate));

        int status = response.getStatus();

        assertThat(status, is(500));
    }

    @Test
    public void findProductById() throws Exception {

        ProductCreate product = createProduct();

        Response response = client.target(String.format(API_URL + "/" + product.getProdId(), RULE.getLocalPort())).request().get();

        int status = response.getStatus();

        if (status != 200) {
            String responseBody = response.readEntity(String.class);
            LOG.warn(responseBody);
        }

        assertThat(status, is(200));

        ProductCreate productReceived = response.readEntity(ProductCreate.class);

        assertThat(productReceived, is(notNullValue()));

    }

    @Test
    public void deleteProduct() throws Exception {
        ProductCreate product = createProduct();

        Response response = client.target(String.format(API_URL + "/" + product.getProdId(), RULE.getLocalPort())).request().delete();

        int status = response.getStatus();

        assertThat(status, is(204));

        response = client.target(String.format(API_URL + "/" + product.getProdId(), RULE.getLocalPort())).request().get();

        status = response.getStatus();

        if (status != 200) {
            String responseBody = response.readEntity(String.class);
            LOG.warn(responseBody);
        }

        assertThat(status, is(404));

    }

    @Test
    public void deleteProductError() throws Exception {

        Response response = client.target(String.format(API_URL + "/" + Long.MAX_VALUE, RULE.getLocalPort())).request().delete();

        int status = response.getStatus();

        assertTrue(status > 200);

    }

    private ProductCreate createProduct() throws Exception {
        ProductCreate product = new ProductCreate();
        product.actor("Scott");
        product.category(1L);
        product.title("Scott Goes to Hollywood");
        product.commonProdId(1L);
        product.price(new BigDecimal(10.00));

        Response response = client.target(String.format(API_URL, RULE.getLocalPort())).request().post(Entity.json(product));

        int status = response.getStatus();

        if (status != 200) {
            String responseBody = response.readEntity(String.class);
            LOG.warn(responseBody);
        }

        assertThat(status, is(200));

        ProductCreate productReceived = response.readEntity(ProductCreate.class);

        assertThat(productReceived, is(notNullValue()));

        return productReceived;
    }

}

