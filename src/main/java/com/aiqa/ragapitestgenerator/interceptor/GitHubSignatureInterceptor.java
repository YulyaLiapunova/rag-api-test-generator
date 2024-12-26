package com.aiqa.ragapitestgenerator.interceptor;

import com.aiqa.ragapitestgenerator.exception.UnauthorizedException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;

@Component
public class GitHubSignatureInterceptor implements HandlerInterceptor {
    private static final String HMAC_SHA256 = "HmacSHA256";
    private final String sharedSecret = "h72HMnWfXQ";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        String signatureHeader = request.getHeader("X-Hub-Signature-256");
//        if (signatureHeader == null || !signatureHeader.startsWith("sha256=")) {
//            throw new UnauthorizedException("Missing or invalid signature header");
//        }
//
//        String rawPayload = readPayload(request);
//        String githubSignature = signatureHeader.replace("sha256=", "");
//        String calculatedHmac = calculateHMAC(sharedSecret, rawPayload);
//
//        System.out.println(githubSignature);
//        System.out.println(calculatedHmac);
//
//        if (!constantTimeCompare(githubSignature, calculatedHmac)) {
//            throw new UnauthorizedException("Invalid signature");
//        }

        return true;
    }

    private String readPayload(HttpServletRequest request) throws Exception {
        StringBuilder payload = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                payload.append(line);
            }
        }
        return payload.toString();
    }

    private String calculateHMAC(String secret, String payload) throws Exception {
        Mac hmacSha256 = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
        hmacSha256.init(secretKey);
        byte[] rawHmac = hmacSha256.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : rawHmac) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    private boolean constantTimeCompare(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
