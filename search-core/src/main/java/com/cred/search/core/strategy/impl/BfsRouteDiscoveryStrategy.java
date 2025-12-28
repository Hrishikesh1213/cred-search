package com.cred.search.core.strategy.impl;

import com.cred.search.core.constants.Constants;
import com.cred.search.core.strategy.RouteDiscoveryStrategy;
import com.cred.search.models.commons.City;
import com.cred.search.models.commons.Route;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("bfsRouteStrategy")
public class BfsRouteDiscoveryStrategy implements RouteDiscoveryStrategy {

    @Override
    public Set<List<Route>> findValidPaths(Set<Route> allPhysicalRoutes, List<City> allCities) {
        Map<Route, List<Route>> routeGraph = buildRouteGraph(allPhysicalRoutes);

        Set<List<Route>> validPaths = new HashSet<>();

        for (int i = 0; i < allCities.size(); i++) {
            for (int j = 0; j < allCities.size(); j++) {
                if (i == j) continue;

                City source = allCities.get(i);
                City dest = allCities.get(j);

                validPaths.addAll(runBfs(routeGraph, source, dest));
            }
        }
        return validPaths;
    }

    private Map<Route, List<Route>> buildRouteGraph(Set<Route> allRoutes) {
        Map<Route, List<Route>> graph = new HashMap<>();
        allRoutes.forEach(r -> graph.put(r, new ArrayList<>()));

        for (Route incoming : allRoutes) {
            for (Route outgoing : allRoutes) {
                if (isValidConnection(incoming, outgoing)) {
                    graph.get(incoming).add(outgoing);
                }
            }
        }
        return graph;
    }

    private boolean isValidConnection(Route in, Route out) {
        if (in.getRouteId().equals(out.getRouteId())) return false;
        if (!in.getDestination().equals(out.getSource())) return false;

        long layover = out.getDepartureTimeEpoch() - in.getArrivalTimeEpoch();
        return layover >= Constants.MIN_LAYOVER_HOURS && layover <= Constants.MAX_LAYOVER_HOURS;
    }

    private Set<List<Route>> runBfs(Map<Route, List<Route>> graph, City source, City destination) {
        Set<List<Route>> suitablePaths = new HashSet<>();
        Queue<List<Route>> queue = new LinkedList<>();

        graph.keySet().stream()
                .filter(r -> r.getSource().equals(source))
                .forEach(r -> queue.add(new ArrayList<>(Collections.singletonList(r))));

        while (!queue.isEmpty()) {
            List<Route> currentPath = queue.poll();
            Route lastLeg = currentPath.get(currentPath.size() - 1);

            if (lastLeg.getDestination().equals(destination)) {
                suitablePaths.add(currentPath);
                continue;
            }

            if (currentPath.size() >= 3) continue;

            List<Route> neighbors = graph.get(lastLeg);
            if (neighbors != null) {
                for (Route neighbor : neighbors) {
                    List<Route> newPath = new ArrayList<>(currentPath);
                    newPath.add(neighbor);
                    queue.add(newPath);
                }
            }
        }
        return suitablePaths;
    }
}