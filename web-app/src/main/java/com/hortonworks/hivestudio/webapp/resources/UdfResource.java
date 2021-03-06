/*
 *
 * HORTONWORKS DATAPLANE SERVICE AND ITS CONSTITUENT SERVICES
 *
 * (c) 2016-2018 Hortonworks, Inc. All rights reserved.
 *
 * This code is provided to you pursuant to your written agreement with Hortonworks, which may be the terms of the
 * Affero General Public License version 3 (AGPLv3), or pursuant to a written agreement with a third party authorized
 * to distribute this code.  If you do not have a written agreement with Hortonworks or with an authorized and
 * properly licensed third party, you do not have any rights to this code.
 *
 * If this code is provided to you under the terms of the AGPLv3:
 * (A) HORTONWORKS PROVIDES THIS CODE TO YOU WITHOUT WARRANTIES OF ANY KIND;
 * (B) HORTONWORKS DISCLAIMS ANY AND ALL EXPRESS AND IMPLIED WARRANTIES WITH RESPECT TO THIS CODE, INCLUDING BUT NOT
 *   LIMITED TO IMPLIED WARRANTIES OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE;
 * (C) HORTONWORKS IS NOT LIABLE TO YOU, AND WILL NOT DEFEND, INDEMNIFY, OR HOLD YOU HARMLESS FOR ANY CLAIMS ARISING
 *   FROM OR RELATED TO THE CODE; AND
 * (D) WITH RESPECT TO YOUR EXERCISE OF ANY RIGHTS GRANTED TO YOU FOR THE CODE, HORTONWORKS IS NOT LIABLE FOR ANY
 *   DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, PUNITIVE OR CONSEQUENTIAL DAMAGES INCLUDING, BUT NOT LIMITED TO,
 *   DAMAGES RELATED TO LOST REVENUE, LOST PROFITS, LOSS OF INCOME, LOSS OF BUSINESS ADVANTAGE OR UNAVAILABILITY,
 *   OR LOSS OR CORRUPTION OF DATA.
 *
 */
package com.hortonworks.hivestudio.webapp.resources;

import java.util.Collection;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.json.simple.JSONObject;
import com.hortonworks.hivestudio.hive.persistence.entities.Udf;
import com.hortonworks.hivestudio.hive.services.UdfService;
import com.hortonworks.hivestudio.common.resource.RequestContext;

import lombok.extern.slf4j.Slf4j;

/**
 * Resource class for working with the hive ide Udfs
 */
@Slf4j
@Path("/udfs")
public class UdfResource {

  private final UdfService udfService;

  @Inject
  public UdfResource(UdfService udfService) {
    this.udfService = udfService;
  }

  /**
   * Gets a setting for the current user
   */
  @GET
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getOne(@PathParam("id") Integer id, @Context RequestContext context) {
    Udf udf = udfService.getOne(id, context.getUsername());
    JSONObject response = new JSONObject();
    response.put("udf", udf);
    return Response.ok(response).status(Response.Status.OK).build();
  }

  /**
   * Gets all the settings for the current user
   */
  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAll(@Context RequestContext context) {
    Collection<Udf> udfs = udfService.getAllForUser(context.getUsername());
    JSONObject response = new JSONObject();
    response.put("udfs", udfs);
    return Response.ok(response).status(Response.Status.OK).build();
  }

  /**
   * Adds a setting for the current user
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response addUdf(UdfService.UdfRequest udfRequest,
                         @Context RequestContext context,
                         @Context HttpServletResponse response,
                         @Context UriInfo uriInfo) {
    Udf udf = udfService.createUdf(udfRequest.getUdf(), context.getUsername());

    response.setHeader("Location",
      String.format("%s/%s", uriInfo.getAbsolutePath().toString(), udf.getId()));

    JSONObject op = new JSONObject();
    op.put("udf", udf);
    return Response.status(Response.Status.CREATED).entity(op).build();
  }

  /**
   * Updates a setting for the current user
   */
  @PUT
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateUdf(@PathParam("id") Integer id, UdfService.UdfRequest udfRequest,
                            @Context RequestContext context,
                            @Context HttpServletResponse response,
                            @Context UriInfo uriInfo) {

    Udf udf = udfService.updateUdf(id, udfRequest.getUdf(), context.getUsername());

    response.setHeader("Location",
      String.format("%s/%s", uriInfo.getAbsolutePath().toString(), udf.getId()));

    JSONObject op = new JSONObject();
    op.put("udf", udf);
    return Response.status(Response.Status.OK).entity(op).build();
  }

  /**
   * Deletes a setting for the current user
   */
  @DELETE
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteUdf(@PathParam("id") Integer id, @Context RequestContext context) {
    udfService.removeUdf(id, context.getUsername());
    return Response.noContent().build();
  }
}
