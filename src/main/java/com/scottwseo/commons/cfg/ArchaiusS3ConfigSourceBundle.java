package com.scottwseo.commons.cfg;

import com.netflix.config.*;
import com.scottwseo.commons.util.S3Util;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.io.InputStream;
import java.net.URL;

import static com.scottwseo.commons.util.StringUtils.isNotEmpty;
import static com.scottwseo.commons.util.EnvVariables.*;

/**
 * Created by seos on 9/8/16.
 */
public class ArchaiusS3ConfigSourceBundle <T extends Configuration>  implements ConfiguredBundle<T> {

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        InputStream is = null;
        String url = CONFIG_URL.value();

        if (isNotEmpty(AWS_PROFILE.value())) {
            String bucket = parseBucket(url);
            String key = parseKey(url);
            is = S3Util.get(bucket, key);
        }
        else {
            is = new URL(url).openStream();
        }

        PolledConfigurationSource s3ConfigurationSource = new InputStreamConfigurationSource(is);

        AbstractPollingScheduler scheduler = new FixedDelayPollingScheduler(0, 60 * 1000, true);

        DynamicConfiguration dynamicConfiguration = new DynamicConfiguration(s3ConfigurationSource, scheduler);

        ConfigurationManager.install(dynamicConfiguration);
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }

    private String parseBucket(String url) {
        //url = "https://s3.amazonaws.com/config.scottwseo.com/dev/config.properties";
        url = url.replace("https://s3.amazonaws.com/", "");

        return url.substring(0, url.indexOf("/"));
    }

    private String parseKey(String url) {
        //url = "https://s3.amazonaws.com/config.scottwseo.com/dev/config.properties";

        url = url.replace("https://s3.amazonaws.com/", "");

        // config.scottwseo.com/dev/config.properties

        return url.substring(url.indexOf("/") + 1, url.length());
    }

}
