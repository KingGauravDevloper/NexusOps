package com.nexusops.aiconcierge.tools;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.web.client.RestTemplate;

import java.util.function.Function;
import java.util.HashMap;

@Configuration
public class BookingTools {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public record ReservationRequest(String resourceId, String userEmail, String start, String end) {}

    @Bean
    @Description("Creates a new room booking reservation in the system. Requires a resource ID, the user's email, and start/end times in ISO-8601 format.")
    public Function<ReservationRequest, String> createReservationTool(RestTemplate restTemplate) {
        return request -> {
            try {
                String url = "http://localhost:8081/api/bookings";
                
                var body = new HashMap<String, String>();
                body.put("resourceId", request.resourceId());
                body.put("userEmail", request.userEmail());
                body.put("startTime", request.start());
                body.put("endTime", request.end());

                // In production, the Concierge would securely pass the user's JWT
                Object response = restTemplate.postForObject(url, body, Object.class);
                return "Successfully booked the room! Confirmation data: " + response.toString();
            } catch (Exception e) {
                return "Failed to book the room. Please check the resource availability and ID, and try again.";
            }
        };
    }
}
