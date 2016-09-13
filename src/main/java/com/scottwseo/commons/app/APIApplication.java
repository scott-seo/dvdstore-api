package com.scottwseo.commons.app;

import be.tomcools.dropwizard.websocket.WebsocketBundle;
import com.google.common.base.MoreObjects;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.netflix.config.ConfigurationManager;
import com.scottwseo.commons.auth.AuthenticationBundle;
import com.scottwseo.commons.cfg.ArchaiusS3ConfigSourceBundle;
import com.scottwseo.commons.guice.ServiceModule;
import com.scottwseo.commons.health.ConfigHealthCheck;
import com.scottwseo.commons.health.DummyHealthCheck;
import com.scottwseo.commons.help.HelpView;
import com.scottwseo.commons.logging.LogEndPoint;
import com.scottwseo.commons.resources.CategoryResource;
import com.scottwseo.commons.resources.HelpResource;
import com.scottwseo.commons.resources.StartupCheckListResource;
import com.scottwseo.commons.togglz.TogglzBundle;
import com.scottwseo.commons.util.AWSCredentialsInitializerBundle;
import com.scottwseo.commons.util.Configs;
import com.scottwseo.commons.util.EnvVariables;
import com.scottwseo.commons.util.PostgreSQLDatabase;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.apache.commons.io.IOUtils;

public class APIApplication extends Application<APIConfiguration> {

    public static void main(final String[] args) throws Exception {
        java.security.Security.setProperty("networkaddress.cache.ttl", "60");
        launch(args);
    }

    public static void launch(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            args = new String[] {"server", "api/conf/api.yml"};
        }
        new APIApplication().run(args);
    }

    @Override
    public String getName() {
        return ConfigurationManager.getConfigInstance().getString(Configs.APP_NAME.key(), "API");
    }

    private WebsocketBundle websocket = new WebsocketBundle();

    @Override
    public void initialize(final Bootstrap<APIConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        if (EnvVariables.check()) {
            // AWSCredentialInitializerBundle is for allowing developers to manually specify the aws
            // credentials using environment variables
            bootstrap.addBundle(new AWSCredentialsInitializerBundle<APIConfiguration>());
            bootstrap.addBundle(new ArchaiusS3ConfigSourceBundle<APIConfiguration>());
        }

        bootstrap.addBundle(new TogglzBundle<APIConfiguration>());

        bootstrap.addBundle(new AuthenticationBundle<APIConfiguration>("Commons"));

        bootstrap.addBundle(new ViewBundle<APIConfiguration>());

        // for assets folder
        bootstrap.addBundle(new AssetsBundle());

        bootstrap.addBundle(new AssetsBundle("/com/scottwseo/commons/help", "/com/scottwseo/commons/help", "index.html", "help"));

        bootstrap.addBundle(new AssetsBundle("/com/scottwseo/commons/swagger", "/com/scottwseo/commons/swagger", "index.html", "swagger"));

        bootstrap.addBundle(new AssetsBundle("/log", "/log", "tail.html", "log"));

        bootstrap.addBundle(websocket);

    }

    @Override
    public void run(final APIConfiguration configuration,
                    final Environment environment) {

        if (EnvVariables.check() && Configs.check() && PostgreSQLDatabase.check()) {

            environment.healthChecks().register("config", new ConfigHealthCheck());

            environment.jersey().register(new HelpResource(new HelpView(), getName(), getAppVersion()));

            Injector injector = Guice.createInjector(new ServiceModule(configuration, environment));

            CategoryResource categoryResource = injector.getInstance(CategoryResource.class);

            environment.jersey().register(categoryResource);

        }
        else {
            environment.jersey().register(new StartupCheckListResource());
            environment.healthChecks().register("dummy", new DummyHealthCheck());
        }

        websocket.addEndpoint(LogEndPoint.class);

    }

    /**
     * Override this method and call getAppVersion(appVersionFileInClassPath, defaultValue) in the overriding
     * method for convenience
     *
     * @return String app version
     */
    protected String getAppVersion() {
        return getAppVersion("app.version", "v1.0.0");
    }

    protected String getAppVersion(String appVersionFileInClassPath, String defaultValue) {

        try {
            ClassLoader classLoader =
                    MoreObjects.firstNonNull(Thread.currentThread().getContextClassLoader(),
                            HelpResource.class.getClassLoader());

            return IOUtils.toString(classLoader.getResourceAsStream(appVersionFileInClassPath));
        }
        catch(Exception ioe) {
            return defaultValue;
        }

    }

}
