package controller;

import exception.NotFoundException;
import exception.ValidationException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import service.InventoryService;
import service.InventoryServiceImpl;

import java.sql.SQLException;

@Path("/inventory")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InventoryController {

    private final InventoryService inventoryService = new InventoryServiceImpl();

    @POST
    @Path("/scan")
    public Response scanBarcode(@QueryParam("barcode") String barcode,
            @QueryParam("storeId") int storeId,
            @QueryParam("quantity") int quantity) {
        try {
            inventoryService.processInventoryScan(barcode, storeId, quantity);
            return Response.ok("Inventory updated").build();
        } catch (ValidationException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error").build();
        }
    }
}