package com.scottwseo.dvdstore.resources;

import com.scottwseo.commons.rest.app.APIConfiguration;
import com.scottwseo.dvdstore.DVDStoreAPIConfiguration;
import com.scottwseo.dvdstore.TestSuiteIT;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Created by seos on 10/5/16.
 */
public class CategoryResourceIT {

    private static Logger LOG = LoggerFactory.getLogger(CategoryResourceIT.class);

    private static final String API_URL = "http://localhost:%d/api/v1/dvdstore/categories";

    @ClassRule
    public static final DropwizardAppRule<DVDStoreAPIConfiguration> RULE = TestSuiteIT.DROPWIZARD;

    private static Client client = TestSuiteIT.CLIENT;

    @Test
    public void categories() throws Exception {

        Response response = client.target(String.format(API_URL, RULE.getLocalPort())).request().get();

        int status = response.getStatus();

        if (status != 200) {
            String responseBody = response.readEntity(String.class);
            LOG.warn(responseBody);
        }

        assertThat(status, is(200));

        List<String> categories = response.readEntity(List.class);

        assertThat(categories, is(notNullValue()));

    }

}

