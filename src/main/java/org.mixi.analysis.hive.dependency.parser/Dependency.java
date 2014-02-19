package org.mixi.analysis.hive.dependency.parser;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by hikaru.ojima on 2014/02/18.
 */
public class Dependency {
    public static final Dependency NULL = new Dependency();
    private ArrayList<String> sources      = new ArrayList<String>();
    private ArrayList<String> destinations = new ArrayList<String>();

    public ArrayList<String> getSources() {
        return sources;
    }

    public ArrayList<String> getDestinations() {
        return destinations;
    }

    public void addSource(String src) {
        sources.add(src);
    }
    public void addDestination(String dst) {
        destinations.add(dst);
    }

    public Dependency merge(Dependency dependency) {
        sources.addAll(dependency.getSources());
        destinations.addAll(dependency.getDestinations());
        return this;
    }

    public String toJson() throws JSONException {
        JSONObject json = new JSONObject();

        JSONArray sources      = new JSONArray(this.sources);
        JSONArray destinations = new JSONArray(this.destinations);

        json.put("sources"     , sources);
        json.put("destinations", destinations);

        return json.toString();
    }
}
