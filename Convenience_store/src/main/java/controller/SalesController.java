package controller;

import java.sql.SQLException;

import entity.Invoice;
import exception.NotFoundException;
import exception.ValidationException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import service.InvoiceService;

@Path("/sales")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SalesController {

    private final InvoiceService invoiceService = new InvoiceService();

    @POST
    @Path("/scan")
    public Response scanBarcode(@QueryParam("storeId") int storeId,
            @QueryParam("employeeId") int employeeId,
            @QueryParam("barcode") String barcode,
            @QueryParam("quantity") int quantity) {
        try {
            Invoice invoice = invoiceService.processSale(storeId, employeeId, barcode, quantity);
            return Response.ok(invoice).build();
        } catch (ValidationException | IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error").build();
        }
    }
}