package com.scottwseo.commons.resources;

import com.scottwseo.commons.auth.User;
import com.scottwseo.commons.help.HelpView;
import com.scottwseo.commons.help.TailView;
import com.scottwseo.commons.util.Configs;
import com.scottwseo.commons.util.EnvVariables;
import io.dropwizard.auth.Auth;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.scottwseo.commons.util.ConfigUtil.isMasked;
import static com.scottwseo.commons.util.ConfigUtil.isRequired;

@Path("/")
public class HelpResource {

    private HelpView helpView;

    private TailView tailView;

    private String appName;

    private String appVersion;

    public HelpResource(String applicationContextPath, String appName, String appVersion) {
        this.appName = appName;
        this.appVersion =  appVersion;
        this.helpView = new HelpView(applicationContextPath);
        this.tailView = new TailView(applicationContextPath);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/help")
    public HelpView help(@Auth User user) {
        return helpView;
    }

    @GET
    @Path("/meta")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Map meta() {

        Map<String, Object> meta = new HashMap();

        meta.put("tag", appVersion);
        meta.put("appname", appName);
        List<Map> configs = new ArrayList<>();

        for (EnvVariables env : EnvVariables.values()) {
            String value = isMasked(env) ? "******" : env.value();
            populateCfg(configs, env.name(), env.key(), value, isRequired(env), "environment");
        }

        for (Configs config : Configs.values()) {
            String value = isMasked(config) ? "******" : config.getString();
            populateCfg(configs, config.name(), config.key(), value, isRequired(config), "dynamic");
        }

        meta.put("configs", configs);

        return meta;

    }

    private void populateCfg(List<Map> configs, String name, String key, String value, boolean required, String type) {
        Map<String, String> map = new HashMap<>();
        map.put("type", type);
        map.put("name", name);
        map.put("key", key);
        map.put("value", value);
        map.put("required", "" + required);
        configs.add(map);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/log")
    public TailView log(@Auth User user) {
        return tailView;
    }

    @GET
    @Path("/metrics")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response metrics(@Context UriInfo uriInfo) {

        String baseUri = uriInfo.getBaseUri().toString();

        // the internal call should not deal network firewall, so localhost is always safe
        String host = "http://localhost";

        String metrics = get("http://localhost:8081/api/v1/dvdstore/metrics");

        Map json = new HashMap<>();
        json.put("baseUri", baseUri);
        json.put("host", host);
        json.put("metrics", metrics);

        return Response.ok().entity(metrics).build();
    }

    protected String get(String url) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("content-type", "application/json")
                .addHeader("cache-control", "no-cache")
                .build();

        String responseBody = null;
        try {
            okhttp3.Response response = client.newCall(request).execute();

            responseBody = response.body().string();

        } catch (IOException e) {
        }

        return responseBody;
    }

}