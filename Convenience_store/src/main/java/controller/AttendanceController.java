package controller;

import entity.Attendance;
import exception.NotFoundException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import service.AttendanceService;
import service.impl.AttendanceServiceImpl;

import java.sql.SQLException;

@Path("/attendance")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AttendanceController {

    private final AttendanceService attendanceService = new AttendanceServiceImpl();

    @POST
    @Path("/scan")
    public Response scanBarcode(@QueryParam("barcode") String barcode) {
        try {
            Attendance attendance = attendanceService.processAttendance(barcode);
            return Response.ok(attendance).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error").build();
        }
    }

    @GET
    @Path("/status/{employeeId}")
    public Response getStatus(@PathParam("employeeId") int employeeId) {
        try {
            String status = attendanceService.getAttendanceStatus(employeeId);
            return Response.ok(status).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error").build();
        }
    }
}
