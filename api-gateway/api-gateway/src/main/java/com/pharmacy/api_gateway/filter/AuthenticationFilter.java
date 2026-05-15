package com.pharmacy.api_gateway.filter;

import com.pharmacy.api_gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    public static class Config {}

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {

            if (validator.isSecured.test(exchange.getRequest())) {

                // Check if Authorization header exists
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("Missing authorization header");
                }

                String authHeader = exchange.getRequest()
                        .getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);

                // Strip "Bearer " prefix to get raw token
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }

                try {
                    // Step 1 — Validate token signature and expiry
                    jwtUtil.validateToken(authHeader);

                    // Step 2 — Extract role from token
                    String role = jwtUtil.extractRole(authHeader);

                    // Step 3 — Get request path and HTTP method
                    String path = exchange.getRequest().getURI().getPath();
                    String method = exchange.getRequest().getMethod().name();

                    // Step 4 — Role based access control
                    // ADMIN can access everything — no restrictions
                    // DOCTOR has limited access — check below
                    if ("DOCTOR".equals(role)) {

                        // Doctors CANNOT access suppliers at all
                        if (path.startsWith("/suppliers")) {
                            throw new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "Access denied: Doctors cannot access supplier data"
                            );
                        }

                        // Doctors can only GET drugs — cannot add, update or delete
                        if (path.startsWith("/drugs") && !method.equals("GET")) {
                            throw new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "Access denied: Doctors can only view drugs"
                            );
                        }

                        // Doctors can only place orders (POST /orders)
                        // and view their own orders (GET /orders/doctor)
                        // Cannot verify, pickup or view all orders
                        if (path.startsWith("/orders")) {
                            boolean isPlaceOrder = method.equals("POST") && path.equals("/orders");
                            boolean isViewOwnOrders = path.startsWith("/orders/doctor");

                            if (!isPlaceOrder && !isViewOwnOrders) {
                                throw new ResponseStatusException(
                                    HttpStatus.FORBIDDEN,
                                    "Access denied: Doctors can only place orders and view their own orders"
                                );
                            }
                        }

                        // Doctors CANNOT access reports
                        if (path.startsWith("/reports")) {
                            throw new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "Access denied: Doctors cannot access reports"
                            );
                        }
                    }

                } catch (ResponseStatusException e) {
                    // Rethrow role-based 403 errors as-is
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException("Unauthorized access to application");
                }
            }

            return chain.filter(exchange);
        });
    }
}