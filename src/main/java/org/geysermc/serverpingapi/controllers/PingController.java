package org.geysermc.serverpingapi.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.geysermc.serverpingapi.services.PingService;
import org.geysermc.serverpingapi.models.CacheData;
import org.geysermc.serverpingapi.models.PingCached;
import org.geysermc.serverpingapi.models.PingResponse;
import org.geysermc.serverpingapi.models.QueryData;
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
            return new PingResponse(false, "Invalid port specified");
        }

        QueryData queryData = null;
        try {
            queryData = new QueryData(hostname, InetAddress.getByName(hostname).getHostAddress(), port);
            if (request != null) request.setAttribute("queryData", queryData);

            PingCached pingData = pingService.getPing(queryData);
            return new PingResponse(pingData.success(), pingData.message(), queryData, pingData.pingData(), new CacheData(pingData.cacheTime()));
        } catch (Exception e) {
            return new PingResponse(false, e.getMessage(), queryData);
        }
    }

    @ExceptionHandler
    public ResponseEntity<?> dataNotFoundExceptionHandling(Exception exception, WebRequest request) {
        return new ResponseEntity<>(new PingResponse(false, exception.getCause().getMessage(), (QueryData) request.getAttribute("queryData", RequestAttributes.SCOPE_REQUEST)), HttpStatus.BAD_REQUEST);
    }
}
