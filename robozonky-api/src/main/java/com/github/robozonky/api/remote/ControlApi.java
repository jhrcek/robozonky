/*
 * Copyright 2017 The RoboZonky Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.robozonky.api.remote;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.github.robozonky.api.remote.entities.PurchaseRequest;
import com.github.robozonky.api.remote.entities.RawInvestment;
import com.github.robozonky.api.remote.entities.Restrictions;
import com.github.robozonky.api.remote.entities.SellRequest;
import com.github.robozonky.internal.api.Defaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
@Produces(Defaults.MEDIA_TYPE)
@Consumes(Defaults.MEDIA_TYPE)
public interface ControlApi {

    Logger LOGGER = LoggerFactory.getLogger(ControlApi.class);

    String ME = "/users/me";
    String INVESTOR_ME = "/investors/me";

    @GET
    @Path(ControlApi.ME + "/logout")
    void logout();

    @GET
    @Path(ControlApi.INVESTOR_ME + "/restrictions")
    Restrictions restrictions();

    @POST
    @Path("/marketplace/investment")
    void invest(RawInvestment investment);

    @POST
    @Path(ControlApi.ME + "/traded-investments")
    void offer(SellRequest sellRequest);

    @POST
    @Path("/smp/investments/{id}/shares")
    void purchase(@PathParam("id") int id, PurchaseRequest purchaseRequest);

    @DELETE
    @Path("/traded-investments/{id}")
    void cancel(@PathParam("id") int id);
}

