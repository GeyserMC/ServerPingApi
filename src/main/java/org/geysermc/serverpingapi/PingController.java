package org.geysermc.serverpingapi;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import java.net.InetAddress;

@RestController
public class PingController {
    @Autowired
    private PingService pingService;

    @GetMapping("/ping")
    public PingResponse ping(@RequestParam(value = "hostname", defaultValue = "") String hostname, @RequestParam(value = "port", defaultValue = "19132") int port, HttpServletRequest request) {
        if (hostname.isBlank()) {
            hostname = request.getRemoteAddr();
        }

        if (port <= 0 || port >= 65535) {
            return new PingResponse(false, "Invalid port specified", null, null);
        }

        QueryData queryData = null;
        try {
            queryData = new QueryData(hostname, InetAddress.getByName(hostname).getHostAddress(), port);
            request.setAttribute("queryData", queryData);
            return new PingResponse(true, "", queryData, pingService.getPing(queryData));
        } catch (Exception e) {
            return new PingResponse(false, e.getMessage(), queryData, null);
        }
    }

    @ExceptionHandler
    public ResponseEntity<?> dataNotFoundExceptionHandling(Exception exception, WebRequest request) {
        return new ResponseEntity<>(new PingResponse(false, exception.getCause().getMessage(), (QueryData) request.getAttribute("queryData",RequestAttributes.SCOPE_REQUEST), null), HttpStatus.BAD_REQUEST);
    }
}
