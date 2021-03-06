/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sebicom.web.controller;

import com.sebicom.domain.JobsResponse;
import com.sebicom.service.LocationDeterminator;
import com.sebicom.service.QueryService;
import com.sebicom.util.Log;
import com.sebicom.util.States;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author mark
 */
@RestController
public class SearchController {

    private final QueryService queryService = new QueryService();

    @RequestMapping("/")
    public JobsResponse search(@RequestParam(value = "q", required = false, defaultValue = "") String query,
            @RequestParam(value = "l", required = false, defaultValue = "") String location,
            @RequestParam(value = "b", required = false, defaultValue = "0") int startIndex,
            @RequestParam(value = "s", required = false, defaultValue = "10") int size) {

        Log.d(SearchController.class, "query: " + query + " location: " + location + " startIndex: " + startIndex + " size: " + size);

        if (query.equals("") && location.equals("")) {
            Log.d(SearchController.class, "query: " + query + " location: " + location + " RETURNING EMPTY ");
            return new JobsResponse();
        }

        location = location.trim();
        query = query.trim();

        // Determine location artifact
        // is it a zipcode, city, state?
        LocationDeterminator locationDeterminator = new LocationDeterminator();
        if (locationDeterminator.isCityAndState(location)) {
            Log.d(SearchController.class, "query: " + query + " location: " + location + " isCityAndState() ");
            String[] locationParts = location.split(",");
            return queryService.queryByCityAndState(query, locationParts[0], locationParts[1], startIndex, size);

        } else if (locationDeterminator.isCityAndState(location, " ")) {
            Log.d(SearchController.class, "query: " + query + " location: " + location + " isCityAndState(SPACE) ");
            String[] locationParts = location.split(" ");
            if (locationParts.length == 2) {
                // Toledo Ohio
                return queryService.queryByCityAndState(query, locationParts[0], locationParts[1], startIndex, size);
            } else if (locationParts.length == 3 && (States.getLowerCaseStates().containsKey(locationParts[1].toLowerCase() + " " + locationParts[2].toLowerCase()))) {
                // Buffalo New York
                return queryService.queryByCityAndState(query, locationParts[0], locationParts[1] + " " + locationParts[2], startIndex, size);
            } else if (locationParts.length == 3 && (States.getLowerCaseStates().containsKey(locationParts[2].toLowerCase()))) {
                // San Jose California
                return queryService.queryByCityAndState(query, locationParts[0] + " " + locationParts[1], locationParts[2], startIndex, size);
            }
        } else if (locationDeterminator.isCityAndStateCode(location)) {
            Log.d(SearchController.class, "query: " + query + " location: " + location + " isCityAndStateCode() ");
            String[] locationParts = location.split(",");
            return queryService.queryByCityAndStateCode(query, locationParts[0], locationParts[1], startIndex, size);

        } else if (locationDeterminator.isCityAndStateCode(location, " ")) {
            Log.d(SearchController.class, "query: " + query + " location: " + location + " isCityAndStateCode(SPACE) ");
            String[] locationParts = location.split(" ");
            if (locationParts.length == 2 && States.getAllStateCodes().contains(locationParts[1].toUpperCase())) {
                // Bayonne NJ
                return queryService.queryByCityAndStateCode(query, locationParts[0], locationParts[1], startIndex, size);
            } else if (locationParts.length == 3 && States.getAllStateCodes().contains(locationParts[2].toUpperCase())) {
                // San Jose CA
                return queryService.queryByCityAndStateCode(query, locationParts[0] + " " + locationParts[1], locationParts[2], startIndex, size);
            }
        } else if (locationDeterminator.isStateCodeOnly(location)) {
            Log.d(SearchController.class, "query: " + query + " location: " + location + " isStateCodeOnly() ");
            // NY
            return queryService.queryByStateCode(query, location, startIndex, size);
        } else if (locationDeterminator.isStateOnly(location)) {
            Log.d(SearchController.class, "query: " + query + " location: " + location + " isStateOnly() ");
            // New Jersey
            return queryService.queryByState(query, location, startIndex, size);
        } else if (locationDeterminator.isZipcode(location)) {
            Log.d(SearchController.class, "query: " + query + " location: " + location + " isZipcode() ");
            // 07002
            return queryService.queryByZipcode(query, location, startIndex, size);
        }

        Log.e(SearchController.class, "query: " + query + " location: " + location + " NO LOCATION FORMAT FOUND ");
        return new JobsResponse();

    }
}
