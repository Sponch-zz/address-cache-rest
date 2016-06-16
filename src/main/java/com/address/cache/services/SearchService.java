package com.address.cache.services;

import com.address.cache.business.AddressCache;
import com.address.cache.model.CacheResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import static java.net.InetAddress.getByName;
import static javax.ws.rs.core.Response.status;

@Component
@Path("/cache")
@Produces("application/json")
@Api(value = "/cache", produces = "application/json", consumes = "application/json")
public class
SearchService {

    private static final Logger LOG = LoggerFactory.getLogger(SearchService.class);

    private AddressCache addressCache = new AddressCache(60, TimeUnit.SECONDS);

    @POST
    @Path("/address")
    @ApiOperation(value = "Post of one Address. Format (000.000.000.000)", notes = "Add one address to the cache")
    public Response add(@QueryParam("address") String address) {

        InetAddress inetAddress = null;

        if(address == null || StringUtils.isEmpty(address)){
            LOG.info("Address Empty");
            return status(400).entity(new CacheResponse("Empty address.")).build();
        }
        try {
            inetAddress = getByName(address);
        }catch(Exception e){
            LOG.info("Error to get address", e);
            return status(400).entity(new CacheResponse("Invalid address: " + address)).build();
        }
        if(this.addressCache.add(inetAddress)){
            LOG.info("Address added with success: " + address);
            return status(201).entity(new CacheResponse("Address added with success: " + address)).build();
        }else{
            LOG.info("Address not added: " + address);
            return status(400).entity(new CacheResponse("Address not added: "  + address)).build();
        }


    }

    @GET
    @Path("/address/peek")
    @ApiOperation(value = "Peek one address", notes = "Peek one address")
    public Response peek() {
        InetAddress address = this.addressCache.peek();
        if(address != null){
            LOG.info("Address peeked: " + address);
            return status(200).entity(new CacheResponse(address.getHostAddress())).build();
        }else{
            LOG.info("Address not cached");
            return status(404).entity(new CacheResponse("Address not cached")).build();
        }
    }

    @GET
    @Path("/address/take")
    @ApiOperation(value = "Take one address", notes = "Take one address")
    public Response take() {
        InetAddress address = null;
        try {
            address = this.addressCache.take();
        }catch(Exception e){
            System.out.println("Thread interrupted");
            return status(500).entity(new CacheResponse("Error in application")).build();
        }
        if(address != null){
            LOG.info("Address took: " + address);
            return status(200).entity(new CacheResponse(address.getHostAddress())).build();
        }else{
            LOG.info("Address Not Cached");
            return status(404).entity(new CacheResponse("Address not cached")).build();
        }
    }

    @DELETE
    @Path("/address/remove")
    @ApiOperation(value = "Remove one address", notes = "Remove one address")
    public Response remove(@QueryParam("address") String address) {
        if(address == null || StringUtils.isEmpty(address)){
            LOG.info("Address Empty");
            return status(400).entity(new CacheResponse("Empty address")).build();

        }
        InetAddress inetAddress = null;
        try {
            inetAddress = getByName(address);
        }catch(Exception e){
            LOG.info("Error to get address", e);
            return status(400).entity(new CacheResponse("Invalid address: " + address)).build();
        }
        if(this.addressCache.remove(inetAddress)){
            LOG.info("Address removed with success: "  + address);
            return status(200).entity(new CacheResponse("Address removed with success: "  + address)).build();
        }else{
            LOG.info("Address Not Removed: "  + address);
            return status(200).entity(new CacheResponse("Address Not Removed: "  + address)).build();
        }
    }
}
